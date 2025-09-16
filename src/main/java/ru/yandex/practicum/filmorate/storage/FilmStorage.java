package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    Film findById(Long id);

    Collection<Film> findAll2();

    Collection<Film> getTop(int count);

    Collection<Film> getFilmsByDirector(Long directorId, String sortBy);
}
