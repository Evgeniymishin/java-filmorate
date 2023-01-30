package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorDbStorage {
    List<Director> getAll();
    Optional<Director> getById(Integer id);
    Director create(Director director);
    Director update(Director director);
    Optional<Director> delete(Integer id);
}
