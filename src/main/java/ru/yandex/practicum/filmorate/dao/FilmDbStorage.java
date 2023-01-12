package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmDbStorage {
    List<Film> getAll();
    Film create(Film film);
    Film update(Film film);
    Optional<Film> getById(Integer id);
    Optional<Film> deleteById(Integer id);
    Optional<Film> addLike(Integer filmId, Integer userId);
    Optional<Film> removeLike(Integer filmId, Integer userId);
    List<Film> getMostPopularFilms(Integer count);
}
