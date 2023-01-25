package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Qualifier("UserDbStorage")
public class UserDbStorageImpl implements UserDbStorage {
    private final JdbcTemplate jdbcTemplate;

    private static User createUser(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("USER_ID");
        String email = rs.getString("EMAIL");
        String login = rs.getString("LOGIN");
        String name = rs.getString("NAME");
        LocalDate birthday = rs.getDate("BIRTHDAY").toLocalDate();

        return new User(id, email, login, name, birthday);
    }

    @Override
    public Optional<User> getById(Integer id) {
        String sqlQuery = "SELECT * FROM USERS WHERE USER_ID = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, UserDbStorageImpl::createUser, id));
        } catch (DataAccessException e) {
            log.warn("Пользователь с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) " +
                "VALUES ( ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"USER_ID"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sqlQuery = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? " +
                "WHERE USER_ID = ?";
        int result = jdbcTemplate.update(sqlQuery,
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (result == 0) {
            throw new NotFoundException("Такого пользователя нет");
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        String sqlQuery = "SELECT * FROM USERS";
        return jdbcTemplate.query(sqlQuery, UserDbStorageImpl::createUser);
    }

    @Override
    public Optional<User> deleteById(Integer id) {
        Optional<User> user = getById(id);
        deleteFromFilmUsersLikes(id);
        deleteFromFriends(id);
        deleteFromUsers(id);
        log.info("Пользователь с id {} удалён", id);
        return user;
    }

    @Override
    public List<Integer> addFriend(Integer currentUserId, Integer friendUserId) {
        String sqlQuery = "MERGE INTO USERFRIENDS (INITIAL_USER_ID, SECOND_USER_ID) " +
                "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, currentUserId, friendUserId);
        log.info("Пользователь с id {} добавил пользователя {} в друзья", currentUserId, friendUserId);
        return List.of(currentUserId, friendUserId);
    }

    @Override
    public List<Integer> deleteFriend(Integer currentUserId, Integer friendUserId) {
        String sqlQuery = "DELETE FROM USERFRIENDS WHERE INITIAL_USER_ID = ? AND SECOND_USER_ID = ?";
        jdbcTemplate.update(sqlQuery, currentUserId, friendUserId);
        log.info("Пользователь с id {} удалил пользователя {} из друзей", currentUserId, friendUserId);
        return List.of(currentUserId, friendUserId);
    }

    @Override
    public List<User> getFriendsListById(Integer id) {
        String sqlQuery = "SELECT USERS.USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY " +
                "FROM USERS " +
                "LEFT JOIN USERFRIENDS f on users.USER_ID = f.SECOND_USER_ID " +
                "where f.INITIAL_USER_ID = ?";

        return jdbcTemplate.query(sqlQuery, UserDbStorageImpl::createUser, id);
    }

    @Override
    public List<User> getCommonFriends(Integer firstUserId, Integer secondUserId) {
        String sqlQuery = "SELECT u.USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY " +
                "FROM USERFRIENDS f " +
                "LEFT JOIN USERS u ON u.USER_ID = f.SECOND_USER_ID " +
                "WHERE f.INITIAL_USER_ID = ? AND f.SECOND_USER_ID IN ( " +
                "SELECT SECOND_USER_ID " +
                "FROM USERFRIENDS AS f " +
                "LEFT JOIN USERS AS u ON u.USER_ID = f.SECOND_USER_ID " +
                "WHERE f.INITIAL_USER_ID = ? )";

        return jdbcTemplate.query(sqlQuery, UserDbStorageImpl::createUser, firstUserId, secondUserId);
    }

    private void deleteFromUsers(Integer id) {
        String sqlQuery = "delete from USERS where USER_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void deleteFromFilmUsersLikes(Integer id) {
        String sqlQuery = "DELETE FROM FILMLIKES WHERE USER_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void deleteFromFriends(Integer id) {
        String sqlQuery = "DELETE FROM USERFRIENDS WHERE INITIAL_USER_ID = ? OR SECOND_USER_ID = ?";
        jdbcTemplate.update(sqlQuery, id, id);
    }
    @Override
    public List<Integer> getRecommendations(Integer id) {
        HashMap<Integer, List<Integer>> userLikes = new HashMap<>();
        List<Integer> recommendFilmsId = new ArrayList<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM FILMLIKES where USER_ID IN " +
                "(SELECT USER_ID FROM FILMLIKES where FILM_ID " +
                "IN (SELECT FILM_ID FROM FILMLIKES where USER_ID=?)) ", id);
        while (userRows.next()) {
            List<Integer> usersFilms = userLikes.getOrDefault(userRows.getInt("USER_ID"), new ArrayList<>());
            usersFilms.add(userRows.getInt("FILM_ID"));
            userLikes.put(userRows.getInt("USER_ID"), usersFilms);
        }
        List<Integer> idUserWithMaxMatchLikes = findUsersIdWithMaxMatchLikes(userLikes, id);
        for (Integer userId : idUserWithMaxMatchLikes) {
            for (Integer filmId : userLikes.get(userId)) {
                if (!userLikes.get(id).contains(filmId)) {
                    recommendFilmsId.add(filmId);
                }
            }
            if (recommendFilmsId.size() != 0) {
                break;
            }
        }
        return recommendFilmsId;
    }

    private List<Integer> findUsersIdWithMaxMatchLikes(HashMap<Integer, List<Integer>> userLikes, int id) {
        Map<Integer, Integer> usersWithMatchLikes = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : userLikes.entrySet()) {
            int matchFilm = 0;
            if (entry.getKey() != id) {
                for (Integer like : entry.getValue()) {
                    if (userLikes.get(id).contains(like)) {
                        matchFilm++;
                    }
                }
                usersWithMatchLikes.put(entry.getKey(), matchFilm);
            }
        }
        List<Integer> sortedUsersIdWithMaxMatchLikes = new ArrayList<>();
        usersWithMatchLikes.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEach(entry -> {
                    sortedUsersIdWithMaxMatchLikes.add(entry.getKey());
                });
        return sortedUsersIdWithMaxMatchLikes;
    }
}
