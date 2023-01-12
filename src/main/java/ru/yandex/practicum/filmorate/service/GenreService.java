package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.GenreDbStorageImpl;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreService {
    private final GenreDbStorageImpl genreDbStorage;

    public List<Genre> getAll() {
        return genreDbStorage.getAll();
    }

    public Genre getById(Integer id) {
        return genreDbStorage.getById(id).orElseThrow(() -> {
            log.warn("Жанр {} не найден.", id);
            throw new NotFoundException("Жанр не найден");
        });
    }
}
