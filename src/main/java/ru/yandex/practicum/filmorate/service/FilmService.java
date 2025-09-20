package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.LikeRepository;

import java.time.Instant;
import java.util.Collection;

import static java.util.stream.Collectors.toSet;
import static ru.yandex.practicum.filmorate.model.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.OperationType.ADD;
import static ru.yandex.practicum.filmorate.model.OperationType.REMOVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final EventService eventService;

    public Film findById(long filmId) {
        Film film = filmRepository.findById(filmId);
        if (film == null) {
            throw new NotFoundException("Film not found: " + filmId);
        }
        if (film.getMpa() != null) {
            film.setMpa(mpaService.findById(film.getMpa().getId()));
        }
        film.setGenres(genreService.findByFilmId(filmId));
        return film;
    }

    public Collection<Film> findAll() {
        return filmRepository.findAll2();
    }

    public Film create(Film film) {
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        return filmRepository.create(film);
    }

    public Film update(Film newFilm) {
        return filmRepository.update(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        validateLikeParams(filmId, userId);
        likeRepository.addLike(filmId, userId);
        eventService.create(new Event(Instant.now().toEpochMilli(), LIKE, ADD, filmId, userId));
    }

    public void removeLike(Long filmId, Long userId) {
        validateLikeParams(filmId, userId);
        likeRepository.removeLike(filmId, userId);
        eventService.create(new Event(Instant.now().toEpochMilli(), LIKE, REMOVE, filmId, userId));
    }

    public Collection<Film> getTop(int count, Long genreId, Integer year) {
        return filmRepository.getTop(count, genreId, year);
    }

    public Collection<Film> getCommon(Long userId, Long filmId) {
        return filmRepository.findCommon(userId, filmId);
    }

    public void validateLikeParams(Long filmId, Long userId) {
        if (filmId == null) {
            throw new IllegalArgumentException("filmId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }

    public void validateMpa(Mpa mpa) {
        if (mpa == null) {
            throw new IllegalArgumentException("Mpa must be provided");
        }
        if (!mpaService.exists(mpa.getId())) {
            throw new NotFoundException("Mpa not found");
        }
    }

    public void validateGenres(Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return; // пустой набор допустим
        }
        Collection<Long> allGenreIds = genreService.findAll().stream()
                .map(Genre::getId)
                .collect(toSet());
        for (Genre g : genres) {
            if (g == null || !allGenreIds.contains(g.getId())) {
                throw new NotFoundException("Genre not found");
            }
        }
    }

    public void delete(long filmId) {
        // findById бросит NotFoundException, если фильма нет
        findById(filmId);
        filmRepository.deleteById(filmId);
    }
}
