package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (films.values().contains(film)){
            log.warn("Film {} already exists", film);
            throw new DuplicatedDataException("Film already exists");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Film {} created", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Film {} does not exist", newFilm);
            throw new ValidationException("Id cannot be null");
        }
        if (films.containsKey(newFilm.getId())) {
            films.replace(newFilm.getId(), newFilm);
            log.info("Film {} updated", newFilm);
            return newFilm;
        }
        else {
            log.warn("Film {} does not exist", newFilm);
            throw new NotFoundException(String.format("Film with id '%s' not found", newFilm.getId()));
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
