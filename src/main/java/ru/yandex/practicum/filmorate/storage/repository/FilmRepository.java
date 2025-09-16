package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
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
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;

    public FilmRepository(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper,
                          DirectorRepository directorRepository, GenreRepository genreRepository,
                          MpaRepository mpaRepository) {
        super(jdbcTemplate);
        this.filmRowMapper = filmRowMapper;
        this.directorRepository = directorRepository;
        this.genreRepository = genreRepository;
        this.mpaRepository = mpaRepository;
    }

    @Override
    public Film findById(Long id) {
        String sql = "SELECT * FROM film WHERE id = ?;";
        var film = findOne(sql, filmRowMapper, id);
        if (film == null) {
            throw new NotFoundException("Film not found");
        }

        film.setMpa(mpaRepository.findById(film.getMpa().getId()));
        film.setGenres(new HashSet<>(genreRepository.findByFilmId(id)));
        film.setDirectors(new HashSet<>(directorRepository.getDirectorsByFilmId(id)));

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM film;";
        Collection<Film> films = findMany(sql, filmRowMapper);
        films.forEach(this::loadFilmRelations);
        return films;
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
        return findById(film.getId());
    }

    private void updateDirectors(Film film) {
        String sql = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getId());
        saveDirectors(film);
    }

    private void updateGenres(Film film) {
        String sql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getId());
        saveGenres(film);
    }

    public Collection<Film> getTop(int count) {
        String sql = """
                SELECT f.*, COUNT(l.user_id) as likes_count
                FROM film f
                LEFT JOIN "like" l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?;
                """;

        Collection<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        films.forEach(this::loadFilmRelations);
        return films;
    }

    @Override
    public Collection<Film> findAll2() {
        String sql = "SELECT * FROM film;";
        Collection<Film> films = findMany(sql, filmRowMapper);
        films.forEach(this::loadFilmRelations);
        return films;
    }

    private void loadFilmRelations(Film film) {
        film.setMpa(mpaRepository.findById(film.getMpa().getId()));
        film.setGenres(new HashSet<>(genreRepository.findByFilmId(film.getId())));
        film.setDirectors(new HashSet<>(directorRepository.getDirectorsByFilmId(film.getId())));
    }

    @Override
    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String sql;
        Object[] params;

        if ("year".equalsIgnoreCase(sortBy)) {
            sql = """
                    SELECT f.*
                    FROM film f
                    JOIN film_director fd ON f.id = fd.film_id
                    WHERE fd.director_id = ?
                    ORDER BY f.release_date ASC
                    """;
            params = new Object[]{directorId};
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            sql = """
                    SELECT f.*, COUNT(l.user_id) as likes_count
                    FROM film f
                    JOIN film_director fd ON f.id = fd.film_id
                    LEFT JOIN "like" l ON f.id = l.film_id
                    WHERE fd.director_id = ?
                    GROUP BY f.id
                    ORDER BY likes_count DESC
                    """;
            params = new Object[]{directorId};
        } else {
            throw new IllegalArgumentException("Invalid sortBy parameter. Must be 'year' or 'likes'.");
        }

        Collection<Film> films = jdbcTemplate.query(sql, filmRowMapper, params);
        films.forEach(this::loadFilmRelations);
        return films;
    }
}