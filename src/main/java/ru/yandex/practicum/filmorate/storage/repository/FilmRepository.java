package ru.yandex.practicum.filmorate.storage.repository;

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
        List<Genre> genres = film.getGenres() == null ? Collections.emptyList() : film.getGenres();
        if (genres.isEmpty()) return;

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

    public Collection<Film> getTop(int count, Long genreId, Integer year) {
        String sql = """
                SELECT f2.id AS film_id, f2.name, f2.description, f2.release_date, f2.duration,
                       f2.mpa_rating_id, mp.NAME as mpa_name, mp.DESCRIPTION as mpa_description,
                       g.id AS genre_id, g.name AS genre_name
                FROM (
                        SELECT f1.*,
                               COUNT(l.user_id) AS c
                        FROM "FILM" f1
                        LEFT JOIN "LIKE" as l ON l.film_id = f1.id
                        LEFT JOIN "FILM_GENRE" as fg ON fg.film_id = f1.id
                        WHERE ( ? IS NULL OR fg.genre_id = ? )
                          AND ( ? IS NULL OR EXTRACT(YEAR FROM f1.release_date) = ? )
                        GROUP BY f1.id
                        ORDER BY c DESC
                        LIMIT ?
                ) as f2
                LEFT JOIN FILM_GENRE fg ON f2.id = fg.film_id
                LEFT JOIN GENRE g ON fg.genre_id = g.id
                LEFT JOIN MPA_RATING as mp ON f2.MPA_RATING_ID = mp.ID;
                """;

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum)
        }, genreId, genreId, year, year, count);

        Map<Long, Film> filmMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Film film = (Film) row[0];
            Genre genre = (Genre) row[1];
            Mpa mpa = (Mpa) row[2];
            film.setMpa(mpa);

            filmMap.computeIfAbsent(film.getId(), id -> {
                film.setGenres(new ArrayList<>());
                return film;
            });

            if (genre != null) {
                List<Genre> list = filmMap.get(film.getId()).getGenres();
                if (!list.contains(genre)) {
                    list.add(genre);
                }
            }
        }
        return filmMap.values();
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

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum)
        });

        Map<Long, Film> filmMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Film film = (Film) row[0];
            Genre genre = (Genre) row[1];
            Mpa mpa = (Mpa) row[2];
            film.setMpa(mpa);

            filmMap.computeIfAbsent(film.getId(), id -> {
                film.setGenres(new ArrayList<>());
                return film;
            });

            if (genre != null) {
                List<Genre> list = filmMap.get(film.getId()).getGenres();
                if (!list.contains(genre)) {
                    list.add(genre);
                }
            }
        }
        return new ArrayList<>(filmMap.values());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM \"FILM\" WHERE id = ?";
        delete(sql, id);
    }

    public Collection<Film> findCommon(long userId, long friendId) {
        String sql = """
                SELECT f2.id AS film_id, f2.name, f2.description, f2.release_date, f2.duration,
                       f2.mpa_rating_id, mp.NAME as mpa_name, mp.DESCRIPTION as mpa_description,
                       g.id AS genre_id, g.name AS genre_name
                FROM (
                        SELECT film_id, COUNT(*) as c
                        FROM "LIKE"
                        WHERE FILM_ID IN (
                            SELECT l1.FILM_ID
                            FROM "LIKE" l1
                            JOIN "LIKE" l2 ON l1.film_id = l2.film_id
                            WHERE l1.user_id = ?
                              AND l2.user_id = ?
                              AND l1.user_id <> l2.user_id
                        )
                        GROUP BY film_id
                ) as f1
                INNER JOIN film as f2 ON f1.film_id = f2.id
                LEFT JOIN FILM_GENRE fg ON f2.id = fg.film_id
                LEFT JOIN GENRE g ON fg.genre_id = g.id
                LEFT JOIN MPA_RATING as mp ON f2.MPA_RATING_ID = mp.ID
                ORDER BY f1.c DESC;
                """;

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum)
        }, userId, friendId);

        Map<Long, Film> filmMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Film film = (Film) row[0];
            Genre genre = (Genre) row[1];
            Mpa mpa = (Mpa) row[2];
            film.setMpa(mpa);

            filmMap.computeIfAbsent(film.getId(), id -> {
                film.setGenres(new ArrayList<>());
                return film;
            });

            if (genre != null) {
                List<Genre> list = filmMap.get(film.getId()).getGenres();
                if (!list.contains(genre)) {
                    list.add(genre);
                }
            }
        }
        return filmMap.values();
    }
}
