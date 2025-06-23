package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class FilmService {
    private FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(Long filmId, Long userId) {
        validateLikeParams(filmId, userId);
        filmStorage.findById(filmId).getLikes().add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateLikeParams(filmId, userId);
        filmStorage.findById(filmId).getLikes().remove(userId);
    }

    public List<Film> getTop(int count) {
        return filmStorage
                .findAll()
                .stream()
                .sorted((x, y) -> x.getLikes().size() - y.getLikes().size())
                .limit(count)
                .collect(toList());
    }

    public void validateLikeParams(Long filmId, Long userId) {
        if (filmId == null) {
            throw new IllegalArgumentException("filmId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }
}
