package ru.yandex.practicum.filmorate.service;

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

import static java.util.stream.Collectors.toSet;
import static ru.yandex.practicum.filmorate.model.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.OperationType.ADD;
import static ru.yandex.practicum.filmorate.model.OperationType.REMOVE;

@Slf4j
@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final LikeRepository likeRepository;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final EventService eventService;
    private final DirectorRepository directorRepository;

    public FilmService(FilmRepository filmRepository, LikeRepository likeRepository,
                       MpaService mpaService, GenreService genreService) {
        this.filmRepository = filmRepository;
        this.likeRepository = likeRepository;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Film findById(long filmId) {
        var film = filmRepository.findById(filmId);
        film.setMpa(mpaService.findById(film.getMpa().getId()));
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

    public void delete(long filmId) {
        if (findById(filmId) == null) {
            throw new NotFoundException("Film not found: " + filmId);
        }
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
