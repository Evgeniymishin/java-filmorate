package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserDbStorage storage;
    private final FeedDbStorage feedStorage;

    public List<User> getAll() {
        return storage.getAll();
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        log.info("Создан пользователь с id = {}", user.getId());
        return storage.create(user);
    }

    public User update(User user) {
        log.info("Обновлен пользователь с id = {}", user.getId());
        return storage.update(user);
    }

    public User getById(Integer id) {
        return storage.getById(id).orElseThrow(() -> {
            log.warn("Пользователь с идентификатором c id {} не найден.", id);
            throw new NotFoundException("Пользователь не найден");
        });
    }

    public List<Integer> addFriend(Integer currentUserId, Integer friendUserId) {
        validateUser(currentUserId);
        validateUser(friendUserId);
        List<Integer> listUsers = storage.addFriend(currentUserId, friendUserId);
        feedStorage.addFeed(currentUserId, friendUserId, Operation.ADD, EventType.FRIEND);
        return listUsers;
    }

    public List<Integer> deleteFriend(Integer currentUserId, Integer friendUserId) {
        validateUser(currentUserId);
        validateUser(friendUserId);
        List<Integer> listUsers = storage.deleteFriend(currentUserId, friendUserId);
        feedStorage.addFeed(currentUserId, friendUserId, Operation.REMOVE, EventType.FRIEND);
        return listUsers;
    }

    public void validateUser(Integer id) {
        if (getById(id) == null) {
            throw new NotFoundException("Такого пользователя нет");
        }
    }

    public List<User> getCommonFriends(Integer firstUserId, Integer secondUserId) {
        validateUser(firstUserId);
        validateUser(secondUserId);
        return storage.getCommonFriends(firstUserId, secondUserId);
    }

    public List<User> getUserFriends(Integer id) {
        validateUser(id);
        return storage.getFriendsListById(id);
    }

    public Optional<User> deleteUser(Integer userId) {
        validateUser(userId);
        return storage.deleteById(userId);
    }

    public List<Feed> getUserFeed(Integer id) {
        validateUser(id);
        return feedStorage.getUserFeed(id);
    }

}
