package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.FilmDbStorageImpl;
import ru.yandex.practicum.filmorate.dao.impl.UserDbStorageImpl;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmService {
    private final FilmDbStorageImpl storage;
    private final UserDbStorageImpl userStorage;
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);
    private static final String MIN_DATE_MSG = "Дата релиза не может быть раньше даты зарождения кино";
    private static final String NO_FILM_MSG = "Такого фильма нет";

    private void validateFilmDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_DATE)) {
            throw new ValidationException(MIN_DATE_MSG);
        }
    }

    public void validateFilmAvailabilityById(Integer id) {
        storage.getById(id).orElseThrow(() -> {
            log.warn("Фильм с идентификатором {} не найден.", id);
            throw new NotFoundException("Фильм не найден");
        });
    }

    public void validateUserAvailabilityById(Integer id) {
        userStorage.getById(id).orElseThrow(() -> {
            log.warn("Пользователь с идентификатором c id {} не найден.", id);
            throw new NotFoundException("Пользователь не найден");
        });
    }

    public Film getById(Integer id) {
        return storage.getById(id).orElseThrow(() -> {
            log.warn("Фильм с идентификатором {} не найден.", id);
            throw new NotFoundException("Фильм не найден");
        });
    }

    public List<Film> getAll() {
        return storage.getAll();
    }

    public Film create(Film film) {
        validateFilmDate(film);
        return storage.create(film);
    }

    public Film update(Film film) {
        validateFilmDate(film);
        return storage.update(film);
    }

    public Film like(Integer filmId, Integer userId) {
        validateFilmAvailabilityById(filmId);
        validateUserAvailabilityById(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return storage.addLike(filmId, userId).orElseThrow();
    }

    public Film dislike(Integer filmId, Integer userId) {
        validateFilmAvailabilityById(filmId);
        validateUserAvailabilityById(userId);
        log.info("Пользователь {} поставил дизлайк фильму {}", userId, filmId);
        return storage.removeLike(filmId, userId).orElseThrow();
    }

    public List<Film> getMostPopularFilms(Integer count) {
        return storage.getMostPopularFilms(count);
    }
}
