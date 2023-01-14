package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UsersDbStorageTest {
    @Autowired
    @Qualifier("UserDbStorage")
    private final UserDbStorage userStorage;
    private User user;

    @BeforeEach
    public void beforeEach() {
        user = new User(0,
                "ivan@ya.ru",
                "ivbest",
                "Ivan",
                LocalDate.of(1988, 12, 26));
    }

    @Test
    public void dtoUsersPublicMethodsTest() {
        userStorage.create(user);
        Optional<User> userFromBd = userStorage.getById(1);
        assertEquals("Ivan", userFromBd.get().getName());
        user.setName("Evgeniy");
        user.setId(1);
        userStorage.update(user);
        userFromBd = userStorage.getById(1);
        assertEquals("Evgeniy", userFromBd.get().getName());
        User user2 = new User(0,
                "sergey@ya.ru",
                "serg",
                "Sergey",
                LocalDate.of(1986, 5, 31));
        userStorage.create(user2);
        List<User> users = new ArrayList<>(userStorage.getAll());
        assertEquals(2, users.size());
        userStorage.deleteById(user.getId());
        users = new ArrayList<>(userStorage.getAll());
        assertEquals(1, users.size());
    }
}