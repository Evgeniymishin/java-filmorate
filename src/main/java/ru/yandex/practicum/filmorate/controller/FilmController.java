package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
public class FilmController {
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);
    private static final String MIN_DATE_MSG = "Дата релиза не может быть раньше даты зарождения кино";
    private static final String NO_FILM_MSG = "Такого фильма нет";
    private final InMemoryFilmStorage storage;

    @Autowired
    public FilmController(InMemoryFilmStorage storage) {
        this.storage = storage;
    }

    private void validateFilmDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_DATE)) {
            log.error("Ошибка валидации: {}", MIN_DATE_MSG);
            throw new ValidationException(MIN_DATE_MSG);
        }
    }

    public void validateFilmAvailability(Film film) {
        if (storage.getAll().get(film.getId() - 1) == null) {
            log.error("Ошибка валидации: {}", NO_FILM_MSG);
            throw new NotFoundException(NO_FILM_MSG);
        }
    }

    @GetMapping("/films")
    public List<Film> getAllFilms() {
        return storage.getAll();
    }

    @PostMapping(value = "/films")
    public Film createFilm(@Valid @RequestBody Film film) {
        validateFilmDate(film);
        log.info("Создан фильм с id = {}", film.getId() + 1);
        return storage.create(film);
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        validateFilmDate(film);
        validateFilmAvailability(film);
        log.info("Обновлен фильм с id = {}", film.getId());
        return storage.update(film);
    }
}
