package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.DirectorDbStorageImpl;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorService {
    private final DirectorDbStorageImpl directorDbStorage;

    public List<Director> getAll() {
        return directorDbStorage.getAll();
    }

    public Director getById(Integer id) {
        return directorDbStorage.getById(id).orElseThrow(() -> {
            log.warn("Режиссер {} не найден.", id);
            throw new NotFoundException("Режиссер не найден");
        });
    }

    public Director create(Director director) {
        log.info("Создан режиссер с id = {}", director.getId());
        return directorDbStorage.create(director);
    }

    public Director update(Director director) {
        log.info("Обновлен режиссер с id = {}", director.getId());
        return directorDbStorage.update(director);
    }

    public Optional<Director> delete(Integer id) {
        log.info("Удален режиссер с id = {}", id);
        return directorDbStorage.delete(id);
    }

}
