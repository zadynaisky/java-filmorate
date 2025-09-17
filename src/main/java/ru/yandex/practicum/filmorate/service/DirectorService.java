package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.repository.DirectorRepository;

import java.util.Collection;

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
        return directorRepository.findById(id);
    }

    public Director create(Director director) {
        log.info("Create director: {}", director);
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Director name cannot be empty");
        }
        return directorRepository.create(director);
    }

    public Director update(Director director) {
        log.info("Update director: {}", director);
        Director existing = directorRepository.findById(director.getId());
        if (existing == null) {
            throw new NotFoundException("Director with id " + director.getId() + " not found");
        }

        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Director name cannot be empty");
        }

        return directorRepository.update(director);
    }

    public void delete(long id) {
        log.info("Delete director with id: {}", id);
        directorRepository.findById(id);
        directorRepository.delete(id);
    }

    public Collection<Director> findByFilmId(long filmId) {
        return directorRepository.findByFilmId(filmId);
    }

    public boolean exists(long id) {
        try {
            findById(id);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
}
