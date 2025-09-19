package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.repository.DirectorRepository;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public Collection<Director> findAll() {
        log.info("Find all directors");
        return directorRepository.findAll();
    }

    public Director findById(long id) {
        log.info("Find director by id: {}", id);
        var director = directorRepository.findById(id);
        if (director == null) {
            throw new NotFoundException("Director with id " + id + " not found");
        }
        return director;
    }

    public Set<Director> findByFilmId(long filmId) {
        log.info("Find directors for film id: {}", filmId);
        return directorRepository.findByFilmId(filmId);
    }

    public Director create(Director director) {
        log.info("Create director: {}", director);
        return directorRepository.create(director);
    }

    public Director update(Director director) {
        log.info("Update director: {}", director);
        if (!exists(director.getId())) {
            throw new NotFoundException("Director with id " + director.getId() + " not found");
        }
        return directorRepository.update(director);
    }

    public void delete(long id) {
        log.info("Delete director with id: {}", id);
        if (!exists(id)) {
            throw new NotFoundException("Director with id " + id + " not found");
        }
        directorRepository.delete(id);
    }

    public boolean exists(long id) {
        return directorRepository.findById(id) != null;
    }
}