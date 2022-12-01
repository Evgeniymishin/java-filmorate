package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
public class UserController {
    private final InMemoryUserStorage storage;

    @Autowired
    public UserController(InMemoryUserStorage storage) {
        this.storage = storage;
    }

    @GetMapping("/users")
    public List <User> getAllUsers() {
        return storage.getAll();
    }

    @PostMapping(value = "/users")
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
}
