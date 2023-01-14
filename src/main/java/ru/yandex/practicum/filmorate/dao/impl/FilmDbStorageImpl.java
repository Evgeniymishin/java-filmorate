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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

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
        return film;
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
                "DURATION = ?, MPA_ID = ?" +
                "WHERE FILM_ID = ?";
        deleteGenres(film);
        addGenres(film);
        int result = jdbcTemplate.update(sqlQuery,
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (result == 0) {
            throw new NotFoundException("Фильм не найден в базе");
        }
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
        deleteFromFilmGenre(id);
        deleteFromFilm(id);
        log.info("Удалён фильм с идентефикатором {}", id);
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

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        String sql = "SELECT FILM.FILM_ID, FILM.NAME, DESCRIPTION, RELEASE_DATE, DURATION, m.MPA_ID, m.NAME " +
                "FROM FILM " +
                "LEFT JOIN FILMLIKES fl ON FILM.FILM_ID = fl.FILM_ID " +
                "LEFT JOIN MPA m on m.MPA_ID = FILM.MPA_ID " +
                "GROUP BY FILM.FILM_ID, fl.FILM_ID IN ( " +
                "SELECT FILM_ID " +
                "FROM FILMLIKES " +
                ") " +
                "ORDER BY COUNT(fl.FILM_ID) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, FilmDbStorageImpl::createFilm, count);
        loadGenres(films);
        return films;
    }

    static Film createFilm(ResultSet rs, Integer rowNum) throws SQLException {
        int id = rs.getInt("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Integer duration = rs.getInt("duration");
        Mpa mpa = new Mpa(rs.getInt("mpa.mpa_id"), rs.getString("mpa.name"));

        return new Film(id, name, description, releaseDate, duration, mpa, new LinkedHashSet<>());
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

    private void deleteFromFilm(Integer id) {
        String sqlQuery = "DELETE FROM FILM WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void deleteFromFilmGenre(Integer id) {
        String sqlQuery = "DELETE FROM FILMGENRE WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void deleteFromFilmLikes(Integer id) {
        String sqlQuery = "DELETE FROM FILMLIKES WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }
}
