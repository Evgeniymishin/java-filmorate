package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorDbStorageImpl implements DirectorDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAll() {
        String sqlQuery = "SELECT * FROM DIRECTOR";

        return jdbcTemplate.query(sqlQuery, DirectorDbStorageImpl::createDirector);
    }

    @Override
    public Optional<Director> getById(Integer id) {
        String sqlQuery = "SELECT * FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, DirectorDbStorageImpl::createDirector, id));
        } catch (DataAccessException e) {
            log.warn("Режиссер с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    @Override
    public Director create(Director director) {
        String sqlQuery = "INSERT INTO DIRECTOR (NAME) " +
                "VALUES ( ? )";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"DIRECTOR_ID"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        return director;
    }

    @Override
    public Director update(Director director) {
        String sqlQuery = "UPDATE DIRECTOR SET NAME = ? " +
                "WHERE DIRECTOR_ID = ?";
        int result = jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        if (result == 0) {
            throw new NotFoundException("Такого режиссера нет");
        }
        return director;
    }

    @Override
    public Optional<Director> delete(Integer id) {
        Optional<Director> director = getById(id);
        String sqlFilmDirector = "DELETE FROM FILMDIRECTOR WHERE DIRECTOR_ID = ?";
        String sqlQuery = "DELETE FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlFilmDirector, id);
        jdbcTemplate.update(sqlQuery, id);
        return director;
    }

    private static Director createDirector(ResultSet rs, int rowNum) throws SQLException {
        Integer id = rs.getInt("director_id");
        String name = rs.getString("name");

        return new Director(id, name);
    }
}
