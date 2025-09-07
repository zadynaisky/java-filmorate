package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper2;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper2;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@Slf4j
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private final FilmRowMapper filmRowMapper;

    public FilmRepository(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper) {
        super(jdbcTemplate);
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    public Film findById(Long id) {
        String sql = "SELECT * FROM film WHERE id = ?;";
        var film = findOne(sql, filmRowMapper, id);
        if (film == null) {
            throw new NotFoundException("Film not found");
        }
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM film;";
        return findMany(sql, filmRowMapper);
    }

    @Override
    public Film create(Film film) {
        String sql = """
                INSERT INTO film (name, description, release_date, duration, mpa_rating_id)
                VALUES (?,?,?,?,?);
                """;
        long id = insert(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        saveGenres(film);
        return film;
    }

    private void saveGenres(Film film) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?,?);";
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = genres.get(i);
                ps.setLong(1, film.getId());
                ps.setLong(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE film
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
                WHERE id = ?;
                """;
        update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        updateGenres(film);
        return film;
    }

    private void updateGenres(Film film) {
        String sql = """
                DELETE FROM film_genre
                WHERE film_id = ?;
                """;
        update(sql, film.getId());
        saveGenres(film);
    }

    public Collection<Film> getTop(int count) {
        String sql = """
                SELECT f2.*
                FROM (
                         SELECT film_id, COUNT(*) as c
                         FROM `like`
                         GROUP BY film_id
                     ) as f1
                         INNER JOIN film as f2 ON f1.film_id = f2.id
                ORDER BY f1.c DESC
                LIMIT ? ;
                """;
        return findMany(sql, filmRowMapper, count);
    }

    public Collection<Film> findAll2() {
        String sql = """
                SELECT f.id AS film_id, f.name, f.description, f.release_date, f.duration,
                                f.mpa_rating_id, mp.NAME as mpa_name, mp.DESCRIPTION as mpa_description,
                                g.id AS genre_id, g.name AS genre_name
                FROM FILM f
                    LEFT JOIN FILM_GENRE fg ON f.id = fg.film_id
                    LEFT JOIN GENRE g ON fg.genre_id = g.id
                    LEFT JOIN MPA_RATING as mp ON f.MPA_RATING_ID = mp.ID;
                """;
        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] {
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum)
        });

        // Группируем фильмы по ID
        Map<Long, Film> filmMap = new HashMap<>();

        for (Object[] row : rows) {
            Film film = (Film) row[0];
            Genre genre = (Genre) row[1];
            Mpa mpa = (Mpa) row[2];
            film.setMpa(mpa);

            if (!filmMap.containsKey(film.getId())) {
                filmMap.put(film.getId(), film);
                film.setGenres(new HashSet<>());
            }

            if (genre != null) {
                filmMap.get(film.getId()).getGenres().add(genre);
            }
        }

        return new ArrayList<>(filmMap.values());
    }
}
