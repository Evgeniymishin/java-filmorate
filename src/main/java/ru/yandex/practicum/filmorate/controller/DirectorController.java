package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorController {
    private final DirectorService service;

    @GetMapping("/directors")
    public List<Director> getAll() {
        return service.getAll();
    }

    @GetMapping("/directors/{id}")
    public Director getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping("/directors")
    public Director create(@Valid @RequestBody Director director) {
        return service.create(director);
    }

    @PutMapping("/directors")
    public Director update(@Valid @RequestBody Director director) {
        return service.update(director);
    }

    @DeleteMapping("/directors/{id}")
    public Optional<Director> delete(@PathVariable Integer id) {
        return service.delete(id);
    }
}
