package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.repository.GenreRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public List<Genre> findByFilmId(long filmId) {
        return new ArrayList<>(genreRepository.findByFilmId(filmId));
    }
}
