package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(Long id);

    Optional<Director> getDirectorById(Long id);

    List<Director> getAllDirectors();

    boolean exists(Long id);

    List<Director> getDirectorsByFilmId(Long filmId);

    void addDirectorToFilm(Long filmId, Long directorId);

    void removeDirectorsFromFilm(Long filmId);
}