package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        getDirectorById(director.getId());
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(Long id) {
        if (!directorStorage.exists(id)) {
            throw new NotFoundException("\n" +
                    "Director with id=\" + id + \" not found");
        }
        directorStorage.deleteDirector(id);
    }

    public Director getDirectorById(Long id) {
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("\n" +
                        "Director with id=\" + id + \" not found"));
    }

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public List<Director> getDirectorsByFilmId(Long filmId) {
        return directorStorage.getDirectorsByFilmId(filmId);
    }
}