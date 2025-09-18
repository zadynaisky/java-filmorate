package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController()
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable("id") long filmId) {
        return filmService.findById(filmId);
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @GetMapping("/popular")
    public Collection<Film> popular(@RequestParam(defaultValue = "10", name = "count") int count) {
        return filmService.getTop(count);
    }

    @GetMapping("/common")
    public Collection<Film> common(@RequestParam(name = "userId") long userId,
                                   @RequestParam(name = "friendId") long friendId) {
        return filmService.getCommon(userId, friendId);
    }

    @PutMapping("/{userId}/like/{id}")
    public void likeFilm(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{userId}/like/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeFilm(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.removeLike(filmId, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") long filmId) {
        filmService.delete(filmId);
    }
}
