package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class FilmController {
    private static int counter = 0;
    private final static LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);
    private final static String MIN_DATE_MSG = "Дата релиза не может быть раньше даты зарождения кино";
    private final static String NO_FILM_MSG = "Такого фильма нет";
    private final Map<Integer, Film> films = new HashMap<>();

    private void validateFilmDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_DATE)) {
            log.error("Ошибка валидации: {}", MIN_DATE_MSG);
            throw new ValidationException(MIN_DATE_MSG);
        }
    }

    @GetMapping("/films")
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping(value = "/films")
    public Film createFilm(@Valid @RequestBody Film film) {
        validateFilmDate(film);
        film.setId(++counter);
        films.put(film.getId(), film);
        log.info("Создан фильм с id = {}", film.getId());
        return film;
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        validateFilmDate(film);
        if (films.get(film.getId()) == null) {
            log.error("Ошибка валидации: {}", NO_FILM_MSG);
            throw new NotFoundException(NO_FILM_MSG);
        }
        log.info("Обновлен фильм с id = {}", film.getId());
        films.put(film.getId(), film);
        return film;
    }
}
