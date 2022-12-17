package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
public class UserController {
    private final InMemoryUserStorage storage;
    private final UserService service;

    @Autowired
    public UserController(InMemoryUserStorage storage, UserService service) {
        this.storage = storage;
        this.service = service;
    }

    @GetMapping("/users")
    public List <User> getAllUsers() {
        return storage.getAll();
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody User user) {
        log.info("Создан пользователь с id = {}", user.getId() + 1);
        return storage.create(user);
    }

    @PutMapping("/users")
    public User updateUser(@Valid @RequestBody User user) {
        if (storage.getAll().get(user.getId() - 1) == null) {
            throw new NotFoundException("Такого пользователя нет");
        }
        log.info("Обновлен пользователь с id = {}", user.getId());
        return storage.update(user);
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Integer id) {
        if (storage.getUsers().get(id) == null) {
            throw new NotFoundException("Такого пользователя нет");
        }
        return storage.getUsers().get(id);
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        if (storage.getUsers().get(id) == null || storage.getUsers().get(friendId) == null) {
            throw new NotFoundException("Такого пользователя нет");
        }
        service.addFriend(id, friendId);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        service.deleteFriend(id, friendId);
    }

    @GetMapping("/users/{id}/friends")
    public List<User> getUserFriends(@PathVariable Integer id) {
        return service.getUserFriends(id);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        if (storage.getUsers().get(id) == null || storage.getUsers().get(otherId) == null) {
            throw new NotFoundException("Такого пользователя нет");
        }
        return service.getCommonFriends(id, otherId);
    }

}
