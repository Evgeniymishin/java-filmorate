package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.GenreDbStorageImpl;
import ru.yandex.practicum.filmorate.dao.impl.MpaDbStorageImpl;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaService {
    private final MpaDbStorageImpl mpaDbStorage;

    public List<Mpa> getAll() {
        return mpaDbStorage.getAll();
    }

    public Mpa getById(Integer id) {
        return mpaDbStorage.getById(id).orElseThrow(() -> {
            log.warn("Рейтинг с id {} не найден.", id);
            throw new NotFoundException("Рейтинг не найден");
        });
    }
}
