package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final InMemoryUserStorage storage;

    @Autowired
    public UserService(InMemoryUserStorage storage) {
        this.storage = storage;
    }

    public void addFriend(Integer currentUserId, Integer friendUserId) {
        storage.getUsers().get(currentUserId).getFriends().add(friendUserId);
        storage.getUsers().get(friendUserId).getFriends().add(currentUserId);
    }

    public void deleteFriend(Integer currentUserId, Integer friendUserId) {
        storage.getUsers().get(currentUserId).getFriends().remove(friendUserId);
        storage.getUsers().get(friendUserId).getFriends().remove(currentUserId);
    }

    public List<User> getCommonFriends(Integer firstUserId, Integer secondUserId) {
        List<Integer> firstUserFriends = new ArrayList<>(storage.getUsers().get(firstUserId).getFriends());
        List<Integer> secondUserFriends = new ArrayList<>(storage.getUsers().get(secondUserId).getFriends());
        firstUserFriends.retainAll(secondUserFriends);
        List<User> result = new ArrayList<>();
        for (Integer friendId: firstUserFriends) {
            result.add(storage.getUsers().get(friendId));
        }
        return result;
    }

    public List<User> getUserFriends(Integer id) {
        List<User> result = new ArrayList<>();
        for (Integer friendId: storage.getUsers().get(id).getFriends()) {
            result.add(storage.getUsers().get(friendId));
        }
        return result;
    }

}
