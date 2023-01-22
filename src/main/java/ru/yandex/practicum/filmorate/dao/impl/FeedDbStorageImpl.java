package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FeedDbStorage;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Qualifier("FeedDbStorage")
public class FeedDbStorageImpl implements FeedDbStorage {

    private final JdbcTemplate jdbcTemplate;

    private static Feed feedRow(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(rs.getInt("EVENT_ID"));
        feed.setUserId(rs.getInt("USER_ID"));
        feed.setEntityId(rs.getInt("ENTITY_ID"));
        feed.setOperation(Operation.valueOf(rs.getString("OPERATION")));
        feed.setEventType(EventType.valueOf(rs.getString("EVENT_TYPE")));
        feed.setTimestamp(rs.getLong("TIMESTAMP"));
        return feed;
    }

    @Override
    public List<Feed> getUserFeed(Integer id) {
        String sqlQuery = "SELECT * FROM FEED WHERE USER_ID = ?";
        return jdbcTemplate.query(sqlQuery, FeedDbStorageImpl::feedRow, id);
    }

    @Override
    public void addFeed(Integer userId, Integer entityId, Operation operation, EventType eventType) {
        String sqlQuery = "INSERT INTO FEED (USER_ID, ENTITY_ID, OPERATION, EVENT_TYPE, TIMESTAMP) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((connection) -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"EVENT_ID"});
            stmt.setInt(1, userId);
            stmt.setInt(2, entityId);
            stmt.setString(3, operation.toString());
            stmt.setString(4, eventType.toString());
            stmt.setLong(5, Instant.now().toEpochMilli());
            return stmt;
        }, keyHolder);
    }

}
