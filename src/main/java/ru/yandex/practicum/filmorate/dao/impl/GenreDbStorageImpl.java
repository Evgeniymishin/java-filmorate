package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageImpl implements GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        String sqlQuery = "SELECT * FROM GENRE";

        return jdbcTemplate.query(sqlQuery, GenreDbStorageImpl::createGenre);
    }

    @Override
    public Optional<Genre> getById(Integer id) {
        String sqlQuery = "SELECT * FROM GENRE WHERE GENRE_ID = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, GenreDbStorageImpl::createGenre, id));
        } catch (DataAccessException e) {
            log.warn("Жанр с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    private static Genre createGenre(ResultSet rs, int rowNum) throws SQLException {
        Integer id = rs.getInt("genre_id");
        String name = rs.getString("name");

        return new Genre(id, name);
    }
}
