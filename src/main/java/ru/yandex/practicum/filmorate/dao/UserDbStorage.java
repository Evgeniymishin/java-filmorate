package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDbStorage {
    User create(User user);
    User update(User user);
    List<User> getAll();
    Optional<User> getById(Integer id);
    Optional<User> deleteById(Integer id);
    List<Integer> addFriend(Integer currentUserId, Integer friendUserId);
    List<Integer> deleteFriend(Integer currentUserId, Integer friendUserId);
    List<User> getFriendsListById(Integer id);
    List<User> getCommonFriends(Integer firstUserId, Integer secondUserId);
}
