package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserController {
    private final UserService service;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return service.getAll();
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody User user) {
        return service.create(user);
    }

    @PutMapping("/users")
    public User updateUser(@Valid @RequestBody User user) {
        return service.update(user);
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
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
        return service.getCommonFriends(id, otherId);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable Integer userId) {
        service.deleteUser(userId);
    }
}
