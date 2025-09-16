package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.LikeRepository;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final UserService userService;

    public Film findById(long filmId) {
        return filmRepository.findById(filmId);
    }

    public Collection<Film> findAll() {
        return filmRepository.findAll2();
    }

    public Film create(Film film) {
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        if (film.getDirectors() != null) {
            film.getDirectors().forEach(d -> directorService.getDirectorById(d.getId()));
        }
        return filmRepository.create(film);
    }

    public Film update(Film newFilm) {
        validateMpa(newFilm.getMpa());
        validateGenres(newFilm.getGenres());
        if (newFilm.getDirectors() != null) {
            newFilm.getDirectors().forEach(d -> directorService.getDirectorById(d.getId()));
        }
        return filmRepository.update(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        validateLikeParams(filmId, userId);
        likeRepository.addLike(userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateLikeParams(filmId, userId);
        likeRepository.removeLike(userId, filmId);
    }

    public Collection<Film> getTop(int count) {
        return filmRepository.getTop(count);
    }

    public void validateLikeParams(Long filmId, Long userId) {
        if (filmId == null) {
            throw new IllegalArgumentException("filmId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (filmRepository.findById(filmId) == null) {
            throw new NotFoundException("Film with ID " + filmId + " not found.");
        }
        if (userService.findById(userId) == null) {
            throw new NotFoundException("User with ID " + userId + " not found.");
        }
    }

    public void validateMpa(Mpa mpa) {
        if (!mpaService.exists(mpa.getId()))
            throw new NotFoundException("Mpa not found");
    }

    public void validateGenres(Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        Collection<Long> allGenreIds = genreService.findAll().stream().map(Genre::getId).collect(toSet());
        genres.forEach(x -> {
            if (!allGenreIds.contains(x.getId()))
                throw new NotFoundException("Genre not found");
        });
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorService.getDirectorById(directorId);
        return filmRepository.getFilmsByDirector(directorId, sortBy);
    }
}
