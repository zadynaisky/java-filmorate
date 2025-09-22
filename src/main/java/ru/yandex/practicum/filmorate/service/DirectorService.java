package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.repository.DirectorRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directors;

    public Director create(Director director) {
        return directors.create(director);
    }

    public Director update(Director director) {
        if (director.getId() == null) throw new NotFoundException("Director's id required");
        return directors.update(director);
    }

    public Director getById(Long id) {
        return directors.findById(id);
    }

    public Collection<Director> getAll() {
        return directors.findAll();
    }

    public void deleteById(Long id) {
        directors.deleteById(id);
    }
}
