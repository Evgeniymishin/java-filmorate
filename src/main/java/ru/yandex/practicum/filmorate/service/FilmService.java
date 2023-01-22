package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.DirectorDbStorageImpl;
import ru.yandex.practicum.filmorate.dao.impl.FilmDbStorageImpl;
import ru.yandex.practicum.filmorate.dao.impl.UserDbStorageImpl;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmService {
    private final FilmDbStorageImpl storage;
    private final UserDbStorageImpl userStorage;
    private final DirectorDbStorageImpl directorStorage;
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);
    private static final String MIN_DATE_MSG = "Дата релиза не может быть раньше даты зарождения кино";

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

    public List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year) {
        return getFilmsByYearFilter(getFilmsByGenreFilter(storage.getAll().stream(), genreId), year)
                .sorted((p0, p1) -> storage.getLikesByFilmId(p1.getId()) - storage.getLikesByFilmId(p0.getId()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private Stream<Film> getFilmsByYearFilter(Stream<Film> stream, Integer year) {
        return (year == null) ? stream : stream.filter(f -> year.equals(f.getReleaseDate().getYear()));
    }

    private Stream<Film> getFilmsByGenreFilter(Stream<Film> stream, Integer genreId) {
        return (genreId == null) ? stream : stream
                .filter(f -> f.getGenres().stream().anyMatch(g -> genreId.equals(g.getId())));
    }

    public List<Film> getAllByDirector(Integer directorId, String sortBy) {
        directorStorage.getById(directorId).orElseThrow(() -> {
            log.warn("Режиссер с id {} не найден", directorId);
            throw new NotFoundException("Режиссер не найден");
        });
        return storage.getAllByDirector(directorId, sortBy);
    }

    public Optional<Film> deleteById(Integer filmId) {
        validateFilmAvailabilityById(filmId);
        return storage.deleteById(filmId);
    }

    public List<Film> getSortedListFilm(String query, List<String> by) {
        return storage.getSortedListFilm(query, by);
    }
}
