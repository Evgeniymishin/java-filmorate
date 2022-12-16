package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FilmService {
    private final InMemoryFilmStorage storage;

    @Autowired
    public FilmService(InMemoryFilmStorage storage) {
        this.storage = storage;
    }

    public void like(Integer filmId, Integer userId) {
        storage.getFilms().get(filmId).like(userId);
    }

    public void dislike(Integer filmId, Integer userId) {
        storage.getFilms().get(filmId).disLike(userId);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        List<Film> films = storage.getAll();
        films.sort(Comparator.comparing(Film::getLikesCount).reversed());
        count = count < films.size() ? count : films.size();
        return new ArrayList<>(films.subList(0, count));
    }
}
