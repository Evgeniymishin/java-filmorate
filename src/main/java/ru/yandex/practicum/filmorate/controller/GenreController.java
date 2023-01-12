package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreController {
    private final GenreService service;

    @GetMapping("/genres")
    public List<Genre> findAll() {
        return service.getAll();
    }

    @GetMapping("/genres/{id}")
    public Genre getById(@PathVariable Integer id) {
        return service.getById(id);
    }
}
