package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageImpl implements MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAll() {
        String sqlQuery = "SELECT * FROM MPA";
        return jdbcTemplate.query(sqlQuery, MpaDbStorageImpl::createMpa);
    }

    @Override
    public Optional<Mpa> getById(Integer id) {
        String sqlQuery = "SELECT * FROM MPA WHERE MPA_ID = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, MpaDbStorageImpl::createMpa, id));
        } catch (DataAccessException e) {
            log.warn("Рейтинг с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }

    private static Mpa createMpa(ResultSet rs, int rowNum) throws SQLException {
        Integer id = rs.getInt("mpa_id");
        String name = rs.getString("name");

        return new Mpa(id, name);
    }
}
