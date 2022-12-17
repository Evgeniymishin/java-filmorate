package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
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
    private final FilmService service;

    @Autowired
    public FilmController(InMemoryFilmStorage storage, FilmService service) {
        this.storage = storage;
        this.service = service;
    }

    private void validateFilmDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_DATE)) {
            throw new ValidationException(MIN_DATE_MSG);
        }
    }

    public void validateFilmAvailability(Film film) {
        if (storage.getAll().get(film.getId() - 1) == null) {
            throw new NotFoundException(NO_FILM_MSG);
        }
    }

    public void validateFilmAvailabilityById(Integer id) {
        if (storage.getAll().get(id - 1) == null) {
            throw new NotFoundException(NO_FILM_MSG);
        }
    }

    @GetMapping("/films")
    public List<Film> getAllFilms() {
        return storage.getAll();
    }

    @PostMapping("/films")
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

    @GetMapping("/films/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        validateFilmAvailabilityById(id);
        return storage.getFilms().get(id);
    }

    @GetMapping("/films/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") String count) {
        return service.getMostPopularFilms(Integer.parseInt(count));
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void like(@PathVariable Integer id, @PathVariable Integer userId) {
        validateFilmAvailabilityById(id);
        validateFilmAvailabilityById(userId);
        service.like(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void dislike(@PathVariable Integer id, @PathVariable Integer userId) {
        validateFilmAvailabilityById(id);
        validateFilmAvailabilityById(userId);
        service.dislike(id, userId);
    }

}
