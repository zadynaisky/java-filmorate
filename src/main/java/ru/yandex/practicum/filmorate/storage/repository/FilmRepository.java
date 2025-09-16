package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@Slf4j
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private final FilmRowMapper filmRowMapper;
    private final DirectorRepository directorRepository;

    public FilmRepository(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper, DirectorRepository directorRepository) {
        super(jdbcTemplate);
        this.filmRowMapper = filmRowMapper;
        this.directorRepository = directorRepository;
    }

    @Override
    public Film findById(Long id) {
        String sql = "SELECT * FROM film WHERE id = ?;";
        var film = findOne(sql, filmRowMapper, id);
        if (film == null) {
            throw new NotFoundException("Film not found");
        }

        var directors = directorRepository.getDirectorsByFilmId(id);
        film.setDirectors(new HashSet<>(directors));

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

        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?,?)";
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

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
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
        updateDirectors(film);
        return film;
    }

    private void updateDirectors(Film film) {
        String sql = "DELETE FROM film_director WHERE film_id = ?";
        update(sql, film.getId()); // Используем update из BaseRepository
        saveDirectors(film);
    }

    private void updateGenres(Film film) {
        String sql = """
                DELETE FROM film_genre
                WHERE film_id = ?;
                """;
        update(sql, film.getId()); // Используем update из BaseRepository
        saveGenres(film);
    }

    public Collection<Film> getTop(int count) {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_rating_id,
                       mr.name AS mpa_name,
                       mr.description AS mpa_description,
                       g.id AS genre_id,
                       g.name AS genre_name,
                       d.id AS director_id,
                       d.name AS director_name,
                       COUNT(l.user_id) AS likes_count
                FROM FILM AS f
                LEFT JOIN MPA_RATING AS mr ON f.mpa_rating_id = mr.id
                LEFT JOIN FILM_GENRE AS fg ON f.id = fg.film_id
                LEFT JOIN GENRE AS g ON fg.genre_id = g.id
                LEFT JOIN FILM_DIRECTOR AS fd ON f.id = fd.film_id
                LEFT JOIN DIRECTOR AS d ON fd.director_id = d.id
                LEFT JOIN "LIKE" AS l ON f.id = l.film_id
                GROUP BY f.id, g.id, d.id
                ORDER BY likes_count DESC
                LIMIT ?;
                """;

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum),
                new DirectorRowMapper2().mapRow(rs, rowNum)
        }, count);

        return mapFilmRowsToCollection(rows);
    }

    @Override
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
                """;

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum),
                new DirectorRowMapper2().mapRow(rs, rowNum)
        });

        return mapFilmRowsToCollection(rows);
    }

    private Collection<Film> mapFilmRowsToCollection(List<Object[]> rows) {
        Map<Long, Film> filmMap = new HashMap<>();

        for (Object[] row : rows) {
            Film film = (Film) row[0];
            Genre genre = (Genre) row[1];
            Mpa mpa = (Mpa) row[2];
            Director director = (Director) row[3];

            film.setMpa(mpa);

            if (!filmMap.containsKey(film.getId())) {
                filmMap.put(film.getId(), film);
                film.setGenres(new HashSet<>());
                film.setDirectors(new HashSet<>());
            }

            if (genre != null && genre.getId() != 0) {
                filmMap.get(film.getId()).getGenres().add(genre);
            }

            if (director != null && director.getId() != null && director.getId() != 0) {
                filmMap.get(film.getId()).getDirectors().add(director);
            }
        }
        return new ArrayList<>(filmMap.values());
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String sql;
        String orderByClause;

        if ("year".equalsIgnoreCase(sortBy)) {
            orderByClause = "ORDER BY f.release_date ASC";
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            orderByClause = "ORDER BY likes_count DESC";
        } else {
            throw new IllegalArgumentException("Invalid sortBy parameter. Must be 'year' or 'likes'.");
        }

        sql = """
                SELECT f.id AS film_id, f.name, f.description, f.release_date, f.duration,
                       f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description,
                       g.id AS genre_id, g.name AS genre_name,
                       d.id AS director_id, d.name AS director_name,
                       COUNT(l.user_id) AS likes_count
                FROM FILM AS f
                JOIN FILM_DIRECTOR AS fd ON f.id = fd.film_id
                JOIN DIRECTOR AS d ON fd.director_id = d.id
                LEFT JOIN MPA_RATING AS mr ON f.mpa_rating_id = mr.id
                LEFT JOIN FILM_GENRE AS fg ON f.id = fg.film_id
                LEFT JOIN GENRE AS g ON fg.genre_id = g.id
                LEFT JOIN "LIKE" AS l ON f.id = l.film_id
                WHERE fd.director_id = ?
                GROUP BY f.id, g.id, d.id
                """ + orderByClause;

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum),
                new DirectorRowMapper2().mapRow(rs, rowNum)
        }, directorId);

        return mapFilmRowsToCollection(rows);
    }
}