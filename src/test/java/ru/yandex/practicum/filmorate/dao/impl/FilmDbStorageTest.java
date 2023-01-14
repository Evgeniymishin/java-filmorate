package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    @Autowired
    @Qualifier("FilmDbStorage")
    private final FilmDbStorage filmStorage;
    private Film film;

    @BeforeEach
    public void beforeEach() {
        film = new Film(0,
                "The Rock",
                "The Film about the situation with hostages in the prison Alcatraz",
                LocalDate.of(1996, 6, 3),
                136,
                new Mpa(1, null),
                new LinkedHashSet<>(List.of(new Genre(1, null), new Genre(2, null))));
    }

    @Test
    public void dtoFilmPublicMethodsTest() {
        filmStorage.create(film);
        Optional<Film> filmFromBd = filmStorage.getById(1);
        assertEquals("The Rock", filmFromBd.get().getName());
        film.setName("Rock");
        film.setId(1);
        filmStorage.update(film);
        filmFromBd = filmStorage.getById(1);
        assertEquals("Rock", filmFromBd.get().getName());
        Film film2 = new Film(0,
                "Titanic",
                "The movie is about the 1912 sinking of the RMS Titanic",
                LocalDate.of(1997, 12, 19),
                194,
                new Mpa(2, null),
                new LinkedHashSet<>(List.of(new Genre(3, null))));
        filmStorage.create(film2);
        List<Film> films = new ArrayList<>(filmStorage.getAll());
        assertEquals(2, films.size());
        filmStorage.deleteById(film.getId());
        films = new ArrayList<>(filmStorage.getAll());
        assertEquals(1, films.size());
    }
}