package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.repository.GenreRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

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
        return new HashSet<>(genreRepository.findByFilmId(filmId));
    }
}
