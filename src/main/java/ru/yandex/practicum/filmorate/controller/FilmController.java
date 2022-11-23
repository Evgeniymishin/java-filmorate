package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;

@RestController
public class FilmController {
    private final List<Film> films = new ArrayList<>();

    @GetMapping("/films")
    public List<Film> getAllFilms() {
        return films;
    }

    @PostMapping(value = "/films")
    public Film createFilm(@RequestBody Film film) {
        films.add(film);
        return film;
    }

    @PutMapping("/films")
    public Film updateFilm(@RequestBody Film film) {
        films.set(film.getId(), film);
        return film;
    }
}
