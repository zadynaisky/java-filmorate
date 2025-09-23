package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.repository.DirectorRepository;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.LikeRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
    private final DirectorRepository directorRepository;

    public Film findById(Long filmId) {
        if (filmId == null) {
            throw new IllegalArgumentException("filmId cannot be null");
        }
        Film film = filmRepository.findById(filmId);
        // обогащаем mpa и жанры
        film.setMpa(mpaService.findById(film.getMpa().getId()));
        Collection<Genre> genres = genreService.findByFilmId(filmId); // может быть List/Collection
        film.setGenres(new LinkedHashSet<>(genres));                  // приводим к Set<Genre>
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

    public Collection<Film> getCommon(Long userId, Long friendId) {
        return filmRepository.findCommon(userId, friendId);
    }

    private void validateLikeParams(Long filmId, Long userId) {
        if (filmId == null) throw new IllegalArgumentException("filmId cannot be null");
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
    }

    private void validateMpa(Mpa mpa) {
        if (mpa == null || mpa.getId() == 0 || !mpaService.exists(mpa.getId())) {
            throw new NotFoundException("Mpa not found");
        }
    }

    private void validateGenres(Collection<Genre> genres) {
        Set<Long> allGenreIds = genreService.findAll().stream().map(Genre::getId).collect(toSet());
        for (Genre g : genres) {
            if (g == null || !allGenreIds.contains(g.getId())) {
                throw new NotFoundException("Genre not found");
            }
        }
    }

    public void delete(Long filmId) {
        if (filmId == null) {
            throw new IllegalArgumentException("filmId cannot be null");
        }
        // findById уже бросит NotFoundException, если фильма нет
        findById(filmId);
        filmRepository.deleteById(filmId);
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        if (!directorRepository.existsById(directorId)) {
            throw new NotFoundException("Director not found: " + directorId);
        }
        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new IllegalArgumentException("Sort must be 'year' or 'likes'");
        }
        return filmRepository.findByDirectorSorted(directorId, sortBy);
    }
}
