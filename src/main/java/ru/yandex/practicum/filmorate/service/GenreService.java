package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.repository.GenreRepository;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class GenreService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GenreService.class);
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Collection<Genre> findAll() {
        log.info("Find all genres");
        return genreRepository.findAll();
    }

    public Genre findById(long id) {
        log.info("Find genre by id: {}", id);
        var genre = genreRepository.findById(id);
        if (genre == null)
            throw new NotFoundException("Genre not found");
        return genre;
    }

    public Set<Genre> findByFilmId(long filmId) {
        return new LinkedHashSet<>(genreRepository.findByFilmId(filmId));
    }
}
