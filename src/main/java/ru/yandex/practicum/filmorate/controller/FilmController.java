package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmController {
    private final FilmService service;

    @GetMapping("/films")
    public List<Film> getAllFilms() {
        return service.getAll();
    }

    @PostMapping("/films")
    public Film createFilm(@Valid @RequestBody Film film) {
        return service.create(film);
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        return service.update(film);
    }

    @GetMapping("/films/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @GetMapping("/films/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") String count) {
        return service.getMostPopularFilms(Integer.parseInt(count));
    }

    @PutMapping("/films/{id}/like/{userId}")
    public Film like(@PathVariable Integer id, @PathVariable Integer userId) {
        return service.like(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public Film dislike(@PathVariable Integer id, @PathVariable Integer userId) {
        return service.dislike(id, userId);
    }

    @GetMapping("/films/director/{directorId}")
    public List<Film> getAllFilmsByDirector(@PathVariable Integer directorId, @RequestParam(defaultValue = "year") String sortBy) {
        return service.getAllByDirector(directorId, sortBy);
    }

    @DeleteMapping("/films/{filmId}")
    public void deleteFilm(@PathVariable Integer filmId) {
        service.deleteById(filmId);
    }

}
