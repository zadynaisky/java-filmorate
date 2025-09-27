package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SortBy;
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
    public Collection<Film> popular(@RequestParam(defaultValue = "10", name = "count") int count,
                                    @RequestParam(name = "genreId", required = false) Long genreId,
                                    @RequestParam(name = "year", required = false) Integer year) {
        return filmService.getTop(count, genreId, year);
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
    public void unlikeFilm(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.removeLike(filmId, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") long filmId) {
        filmService.delete(filmId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getByDirector(@PathVariable Long directorId,
                                          @RequestParam(name = "sortBy") String sortBy) {
        return filmService.getFilmsByDirector(directorId, SortBy.fromString(sortBy));
    }

    @GetMapping("/search")
    public Collection<Film> search(@RequestParam String query, @RequestParam String by) {
        return filmService.search(query, by);
    }
}
