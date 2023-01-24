package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.util.List;

public interface FeedDbStorage {
    List<Feed> getUserFeed(Integer id);
    void addFeed(Integer userId, Integer entityId, Operation operation, EventType eventType);
}
