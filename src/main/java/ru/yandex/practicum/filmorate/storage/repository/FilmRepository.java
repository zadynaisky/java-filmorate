package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper2;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper2;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper2;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper2;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@Slf4j
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private final FilmRowMapper filmRowMapper;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    public FilmRepository(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper,
                          MpaService mpaService, GenreService genreService, DirectorService directorService) {
        super(jdbcTemplate);
        this.filmRowMapper = filmRowMapper;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
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
        saveDirectors(film);
        return film;
    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?,?);";
        List<Director> directors = new ArrayList<>(film.getDirectors());
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = directors.get(i);
                ps.setLong(1, film.getId());
                ps.setLong(2, director.getId());
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        });
    }

    @Override
    public Film update(Film newFilm) {
        String sql = """
                UPDATE film
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
                WHERE id = ?;
                """;
        int rowsUpdated = jdbcTemplate.update(sql, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration(), newFilm.getMpa().getId(), newFilm.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Film with id " + newFilm.getId() + " not found");
        }

        updateGenres(newFilm);
        updateDirectors(newFilm);
        return findById(newFilm.getId());
    }

    private void updateDirectors(Film film) {
        String sql = "DELETE FROM film_director WHERE film_id = ?;";
        jdbcTemplate.update(sql, film.getId());
        saveDirectors(film);
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

    public Film updateDirectorsOnly(Film film) {
        String deleteSql = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());
        saveDirectors(film);
        return findById(film.getId());
    }

    private void updateGenres(Film film) {
        String sql = """
                DELETE FROM film_genre
                WHERE film_id = ?;
                """;
        jdbcTemplate.update(sql, film.getId());
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
                                g.id AS genre_id, g.name AS genre_name,
                                d.id AS director_id, d.name AS director_name
                FROM FILM f
                    LEFT JOIN FILM_GENRE fg ON f.id = fg.film_id
                    LEFT JOIN GENRE g ON fg.genre_id = g.id
                    LEFT JOIN MPA_RATING as mp ON f.MPA_RATING_ID = mp.ID
                    LEFT JOIN FILM_DIRECTOR fd ON f.id = fd.film_id
                    LEFT JOIN DIRECTOR d ON fd.director_id = d.id
                ORDER BY f.id
                """;

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum),
                new DirectorRowMapper2().mapRow(rs, rowNum)
        });

        Map<Long, Film> filmMap = new HashMap<>();

        for (Object[] row : rows) {
            Film film = (Film) row[0];
            Genre genre = (Genre) row[1];
            Mpa mpa = (Mpa) row[2];
            Director director = (Director) row[3];

            if (!filmMap.containsKey(film.getId())) {
                film.setMpa(mpa);
                film.setGenres(new HashSet<>());
                film.setDirectors(new HashSet<>());
                filmMap.put(film.getId(), film);
            }

            Film currentFilm = filmMap.get(film.getId());

            if (genre != null && genre.getId() != 0) {
                currentFilm.getGenres().add(genre);
            }

            if (director != null && director.getId() != 0) {
                currentFilm.getDirectors().add(director);
            }
        }

        return new ArrayList<>(filmMap.values());
    }

    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        if (!directorService.exists(directorId)) {
            return Collections.emptyList();
        }

        String sql;
        if ("year".equals(sortBy)) {
            sql = """
            SELECT f.* 
            FROM film f
            INNER JOIN film_director fd ON f.id = fd.film_id
            WHERE fd.director_id = ?
            ORDER BY f.release_date ASC
            """;
        } else {
            sql = """
            SELECT f.*, COUNT(l.user_id) as like_count
            FROM film f
            INNER JOIN film_director fd ON f.id = fd.film_id
            LEFT JOIN `like` l ON f.id = l.film_id
            WHERE fd.director_id = ?
            GROUP BY f.id
            ORDER BY like_count DESC, f.id ASC
            """;
        }

        try {
            Collection<Film> films = jdbcTemplate.query(sql, filmRowMapper, directorId);

            if (films == null || films.isEmpty()) {
                return Collections.emptyList();
            }

            for (Film film : films) {
                if (film != null) {
                    Mpa mpa = mpaService.findById(film.getMpa().getId());
                    film.setMpa(mpa);
                    Set<Genre> genres = new HashSet<>(genreService.findByFilmId(film.getId()));
                    film.setGenres(genres);
                    Set<Director> directors = new HashSet<>(directorService.findByFilmId(film.getId()));
                    film.setDirectors(directors);
                }
            }

            return films;
        } catch (Exception e) {
            log.error("Error getting films by director {} with sort {}", directorId, sortBy, e);
            return Collections.emptyList();
        }
    }
}