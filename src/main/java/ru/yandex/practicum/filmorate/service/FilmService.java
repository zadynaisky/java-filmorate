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

    public void addLike(Long userId, Long movieId) {
        validateLikeParams(userId, movieId);
        filmStorage.findById(movieId).getLikes().add(userId);
    }

    public void removeLike(Long userId, Long movieId) {
        validateLikeParams(userId, movieId);
        filmStorage.findById(movieId).getLikes().remove(userId);
    }

    public List<Film> getTop(int count) {
        return filmStorage
                .findAll()
                .stream()
                .sorted((x, y) -> x.getLikes().size() - y.getLikes().size())
                .limit(count)
                .collect(toList());
    }

    public void validateLikeParams(Long userId, Long movieId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (movieId == null) {
            throw new IllegalArgumentException("movieId cannot be null");
        }
    }
}
