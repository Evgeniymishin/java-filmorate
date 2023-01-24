package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Qualifier("FilmDbStorage")
public class FilmDbStorageImpl implements FilmDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public List<Film> getAll() {
        String sqlQuery = "SELECT FILM.*, m.* " +
                "FROM FILM " +
                "JOIN MPA m ON m.MPA_ID = FILM.mpa_id";
        List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorageImpl::createFilm);
        loadGenres(films);
        loadDirectors(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO FILM (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        addGenres(film);
        addDirectors(film);
        log.info("Создан фильм с идентификатором {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
                "DURATION = ?, MPA_ID = ?" +
                "WHERE FILM_ID = ?";
        deleteGenres(film);
        deleteDirectors(film);
        addGenres(film);
        addDirectors(film);
        int result = jdbcTemplate.update(sqlQuery,
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (result == 0) {
            throw new NotFoundException("Фильм не найден в базе");
        }
        log.info("Обновлен фильм с идентификатором {}", film.getId());
        return film;
    }

    @Override
    public Optional<Film> getById(Integer id) {
        String sqlQuery = "SELECT FILM.*, m.* " +
                "FROM FILM " +
                "JOIN MPA m ON m.MPA_ID = FILM.MPA_ID " +
                "WHERE FILM.FILM_ID = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, FilmDbStorageImpl::createFilm, id);
            loadGenres(Collections.singletonList(film));
            loadDirectors(Collections.singletonList(film));
            return Optional.ofNullable(film);
        } catch (DataAccessException e) {
            log.warn("Фильм с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Film> deleteById(Integer id) {
        Optional<Film> film = getById(id);
        deleteFromFilmLikes(id);
        deleteGenres(film.get());
        deleteDirectors(film.get());
        deleteFromFilm(id);
        log.info("Удалён фильм с идентификатором {}", id);
        return film;
    }

    @Override
    public Optional<Film> addLike(Integer filmId, Integer userId) {
        String sqlQuery = "MERGE INTO FILMLIKES (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        return getById(filmId);
    }

    @Override
    public Optional<Film> removeLike(Integer filmId, Integer userId) {
        String sqlQuery = "DELETE FROM FILMLIKES " +
                "WHERE FILM_ID = ? AND USER_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        return getById(filmId);
    }

    static Film createFilm(ResultSet rs, Integer rowNum) throws SQLException {
        int id = rs.getInt("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Integer duration = rs.getInt("duration");
        Mpa mpa = new Mpa(rs.getInt("mpa.mpa_id"), rs.getString("mpa.name"));

        return new Film(id, name, description, releaseDate, duration, mpa, new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    private void loadGenres(List<Film> films) {
        String sqlGenres = "SELECT FILM_ID, g.* " +
                "FROM FILMGENRE " +
                "JOIN GENRE g ON g.genre_id = FILMGENRE.GENRE_ID " +
                "WHERE FILM_ID IN (:ids)";
        List<Integer> ids = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film, (a, b) -> b));
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        SqlRowSet sqlRowSet = namedJdbcTemplate.queryForRowSet(sqlGenres, parameters);
        while (sqlRowSet.next()) {
            int filmId = sqlRowSet.getInt("film_id");
            int genreId = sqlRowSet.getInt("genre_id");
            String name = sqlRowSet.getString("name");
            filmMap.get(filmId).getGenres().add(new Genre(genreId, name));
        }
        films.forEach(film -> film.getGenres().addAll(filmMap.get(film.getId()).getGenres()));
    }

    private void addGenres(Film film) {
        if (film.getGenres() != null) {
            String updateGenres = "INSERT INTO FILMGENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(
                    updateGenres, film.getGenres(), film.getGenres().size(),
                    (ps, genre) -> {
                        ps.setInt(1, film.getId());
                        ps.setInt(2, genre.getId());
                    });
        } else film.setGenres(new LinkedHashSet<>());
    }

    private void deleteGenres(Film film) {
        String deleteGenres = "DELETE FROM FILMGENRE WHERE FILM_ID = ?";
        jdbcTemplate.update(deleteGenres, film.getId());
    }

    private void addDirectors(Film film) {
        if (film.getDirectors() != null) {
            String sqlQuery = "INSERT INTO FILMDIRECTOR (FILM_ID, DIRECTOR_ID) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(
                    sqlQuery, film.getDirectors(), film.getDirectors().size(),
                    (ps, dir) -> {
                        ps.setInt(1, film.getId());
                        ps.setInt(2, dir.getId());
                    });
        } else {
            film.setDirectors(new LinkedHashSet<>());
        }
    }

    private void deleteDirectors(Film film) {
        String sqlQuery = "DELETE FROM FILMDIRECTOR WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
    }

    private void loadDirectors(List<Film> films) {
        String sqlQuery = "SELECT FILM_ID, d.* " +
                "FROM FILMDIRECTOR " +
                "JOIN DIRECTOR d ON d.director_id = FILMDIRECTOR.DIRECTOR_ID " +
                "WHERE FILM_ID IN (:ids)";
        List<Integer> ids = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film, (a, b) -> b));
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        SqlRowSet sqlRowSet = namedJdbcTemplate.queryForRowSet(sqlQuery, parameters);
        while (sqlRowSet.next()) {
            int filmId = sqlRowSet.getInt("film_id");
            int directorId = sqlRowSet.getInt("director_id");
            String name = sqlRowSet.getString("name");
            filmMap.get(filmId).getDirectors().add(new Director(directorId, name));
        }
        films.forEach(film -> film.getDirectors().addAll(filmMap.get(film.getId()).getDirectors()));
    }

    @Override
    public List<Film> getAllByDirector(Integer directorId, String sortBy) {
        String sortedByLikes = "SELECT f.*, m.*, fd.DIRECTOR_ID " +
                "FROM FILMDIRECTOR fd " +
                "JOIN FILM f on f.FILM_ID = fd.FILM_ID " +
                "JOIN MPA m on f.MPA_ID = m.MPA_ID " +
                "LEFT JOIN FILMLIKES fl on f.FILM_ID = fl.film_id " +
                "WHERE DIRECTOR_ID = ? " +
                "GROUP BY f.FILM_ID, fl.FILM_ID IN ( " +
                "SELECT FILM_ID " +
                "FROM FILMLIKES " +
                ") " +
                "ORDER BY COUNT(fl.FILM_ID) DESC";
        String sortedByYear = "SELECT F.*, M.*, fd.DIRECTOR_ID " +
                "FROM FILMDIRECTOR fd " +
                "JOIN FILM f on f.FILM_ID = fd.FILM_ID " +
                "JOIN MPA m on f.mpa_id = m.MPA_ID " +
                "WHERE DIRECTOR_ID = ? " +
                "GROUP BY f.FILM_ID, f.RELEASE_DATE " +
                "ORDER BY EXTRACT(YEAR FROM cast(f.RELEASE_DATE AS date))";
        List<Film> films = new ArrayList<>();
        if (sortBy.equals("likes")) {
            films = jdbcTemplate.query(sortedByLikes, FilmDbStorageImpl::createFilm, directorId);
        } else {
            films = jdbcTemplate.query(sortedByYear, FilmDbStorageImpl::createFilm, directorId);
        }
        loadGenres(films);
        loadDirectors(films);
        return films;
    }

    private void deleteFromFilm(Integer id) {
        String sqlQuery = "DELETE FROM FILM WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void deleteFromFilmLikes(Integer id) {
        String sqlQuery = "DELETE FROM FILMLIKES WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        String getCommonFilms = "WITH sort AS " +
                "    (SELECT f.FILM_ID AS sort_id " +
                "            FROM FILM f " +
                "            LEFT JOIN FILMLIKES fl on f.FILM_ID = fl.film_id " +
                "            GROUP BY f.FILM_ID, fl.FILM_ID IN " +
                "            (SELECT FILM_ID " +
                "            FROM FILMLIKES) " +
                "            ORDER BY COUNT(fl.FILM_ID) DESC) " +
                "SELECT F.*, M.*, s.* " +
                "FROM FILMLIKES FL " +
                "         JOIN FILM F on FL.FILM_ID = F.FILM_ID " +
                "         JOIN MPA m on f.MPA_ID = m.MPA_ID " +
                "         JOIN sort s on FL.FILM_ID = s.sort_id " +
                "WHERE (FL.USER_ID = ? OR FL.USER_ID = ?) AND " +
                "        FL.FILM_ID IN (SELECT FL.FILM_ID " +
                "                       FROM FILMLIKES FL " +
                "                       WHERE FL.USER_ID = ? OR FL.USER_ID = ? " +
                "                       GROUP BY FL.FILM_ID " +
                "                       having count(*) > 1) " +
                "GROUP BY FL.FILM_ID " +
                "ORDER BY s.sort_id DESC";
        List<Film> films = jdbcTemplate.query(getCommonFilms, FilmDbStorageImpl::createFilm, userId, friendId, userId, friendId);
        loadGenres(films);
        loadDirectors(films);
        return films;
    }

    public int getLikesByFilmId(Integer id) {
        String sqlQuery = "SELECT USER_ID FROM FILMLIKES WHERE FILM_ID = ?";
        return jdbcTemplate.queryForList(sqlQuery, id).size();
    }
}
