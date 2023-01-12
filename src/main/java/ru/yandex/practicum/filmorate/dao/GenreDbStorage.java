package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreDbStorage {
    List<Genre> getAll();
    Optional<Genre> getById(Integer id);
}
