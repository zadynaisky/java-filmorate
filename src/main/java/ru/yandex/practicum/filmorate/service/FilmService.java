package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.repository.FilmRepository;
import ru.yandex.practicum.filmorate.storage.repository.LikeRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    public Film findById(long filmId) {
        var film = filmRepository.findById(filmId);
        film.setMpa(mpaService.findById(film.getMpa().getId()));
        film.setGenres(genreService.findByFilmId(filmId));
        film.setDirectors(new HashSet<>(directorService.findByFilmId(filmId)));
        return film;
    }

    public Collection<Film> findAll() {
        return filmRepository.findAll2();
    }

    public Film create(Film film) {
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        validateDirectors(film.getDirectors());
        return filmRepository.create(film);
    }

    public Film update(Film newFilm) {
        Film existingFilm = filmRepository.findById(newFilm.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Film with id " + newFilm.getId() + " not found");
        }
        validateMpa(newFilm.getMpa());
        validateGenres(newFilm.getGenres());
        validateDirectors(newFilm.getDirectors());

        return filmRepository.update(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        validateLikeParams(filmId, userId);
        likeRepository.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateLikeParams(filmId, userId);
        likeRepository.removeLike(filmId, userId);
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
    }

    public void validateMpa(Mpa mpa) {
        if (!mpaService.exists(mpa.getId()))
            throw new NotFoundException("Mpa not found");
    }

    public void validateGenres(Collection<Genre> genres) {
        Collection<Long> allGenreIds = genreService.findAll().stream().map(Genre::getId).collect(toSet());
        genres.forEach(x -> {
            if (!allGenreIds.contains(x.getId()))
                throw new NotFoundException("Genre not found");
        });
    }

    public void validateDirectors(Set<Director> directors) {
        if (directors != null) {
            for (Director director : directors) {
                if (director.getId() == null) {
                    throw new ValidationException("Director ID cannot be null");
                }
                if (!directorService.exists(director.getId())) {
                    throw new NotFoundException("Director with id " + director.getId() + " not found");
                }
            }
        }
    }

    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        if (!directorService.exists(directorId)) {
            throw new NotFoundException("Director not found");
        }

        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new ValidationException("Sort parameter must be 'year' or 'likes'");
        }

        return filmRepository.getFilmsByDirector(directorId, sortBy);
    }
}
