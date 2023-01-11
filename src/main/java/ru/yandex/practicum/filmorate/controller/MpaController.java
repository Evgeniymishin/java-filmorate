package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaController {
    private final MpaService service;

    @GetMapping("/mpa")
    public List<Mpa> findAll() {
        return service.getAll();
    }

    @GetMapping("/mpa/{id}")
    public Mpa getById(@PathVariable Integer id) {
        return service.getById(id);
    }
}
