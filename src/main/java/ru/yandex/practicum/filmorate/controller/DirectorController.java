package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Slf4j
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> findAll() {
        log.info("Get all directors");
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable("id") long id) {
        log.info("Get director by id: {}", id);
        return directorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@Valid @RequestBody Director director) {
        log.info("Create director: {}", director);
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        log.info("Update director: {}", director);
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") long id) {
        log.info("Delete director with id: {}", id);
        directorService.delete(id);
    }
}



