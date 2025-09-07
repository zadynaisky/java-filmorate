package ru.yandex.practicum.filmorate.storage.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.repository.GenreRepository;
import ru.yandex.practicum.filmorate.storage.repository.MpaRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        var film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", LocalDate.class));
        film.setDuration(rs.getInt("duration"));
        var mpa = new Mpa();
        mpa.setId(rs.getLong("mpa_rating_id"));
        film.setMpa(mpa);
        return film;
    }
}
