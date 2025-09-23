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
        saveDirectors(film);
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
        updateDirectors(film);
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

            if (!filmMap.containsKey(film.getId())) {
                filmMap.put(film.getId(), film);
                film.setGenres(new HashSet<>());
            }

            if (genre != null) {
                filmMap.get(film.getId()).getGenres().add(genre);
            }
        }
        loadDirectorsFor(filmMap);
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
        loadDirectorsFor(filmMap);
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

            if (!filmMap.containsKey(film.getId())) {
                filmMap.put(film.getId(), film);
                film.setGenres(new HashSet<>());
            }

            if (genre != null) {
                filmMap.get(film.getId()).getGenres().add(genre);
            }
        }
        loadDirectorsFor(filmMap);
        return filmMap.values();
    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?,?);";
        List<Director> dirs = new ArrayList<>(film.getDirectors());

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director d = dirs.get(i);
                ps.setLong(1, film.getId());
                ps.setLong(2, d.getId());
            }

            @Override
            public int getBatchSize() {
                return dirs.size();
            }
        });
    }

    private void updateDirectors(Film film) {
        String del = "DELETE FROM film_director WHERE film_id = ?;";
        jdbcTemplate.update(del, film.getId());
        saveDirectors(film);
    }

    private void loadDirectorsFor(Map<Long, Film> filmMap) {
        if (filmMap.isEmpty()) return;

        String placeholders = String.join(",", Collections.nCopies(filmMap.size(), "?"));
        String sql = """
                SELECT fd.film_id, d.id, d.name
                FROM film_director fd
                JOIN director d ON d.id = fd.director_id
                WHERE fd.film_id IN (""" + placeholders + ")";
        Object[] args = filmMap.keySet().toArray();

        jdbcTemplate.query(sql, rs -> {
            long filmId = rs.getLong("film_id");
            var dir = new Director();
            dir.setId(rs.getLong("id"));
            dir.setName(rs.getString("name"));
            filmMap.get(filmId).getDirectors().add(dir);
        }, args);
    }

    // Разбил на несколько частей для удобства
    private List<Long> findFilmIdsByDirectorOrderByYear(Long directorId) {
        String sql = """
                SELECT f.id
                FROM film f
                JOIN film_director fd ON fd.film_id = f.id
                WHERE fd.director_id = ?
                ORDER BY f.release_date, f.id
                """;
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong("id"), directorId);
    }

    private List<Long> findFilmIdsByDirectorOrderByLikes(Long directorId) {
        String sql = """
                SELECT f.id
                FROM film f
                JOIN film_director fd ON fd.film_id = f.id
                LEFT JOIN "LIKE" l ON l.film_id = f.id
                WHERE fd.director_id = ?
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC, f.id
                """;
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong("id"), directorId);
    }

    private Collection<Film> findRichByIdsPreservingOrder(List<Long> ids) {
        if (ids.isEmpty()) return List.of();

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = """
        SELECT f.id AS film_id, f.name, f.description, f.release_date, f.duration,
               f.mpa_rating_id, mp.NAME as mpa_name, mp.DESCRIPTION as mpa_description,
               g.id AS genre_id, g.name AS genre_name
        FROM film f
        LEFT JOIN film_genre fg ON f.id = fg.film_id
        LEFT JOIN genre g ON fg.genre_id = g.id
        LEFT JOIN mpa_rating mp ON f.mpa_rating_id = mp.id
        WHERE f.id IN (""" + placeholders + ")";
        Object[] args = ids.toArray();

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{
                new FilmRowMapper2().mapRow(rs, rowNum),
                new GenreRowMapper2().mapRow(rs, rowNum),
                new MpaRowMapper2().mapRow(rs, rowNum)
        }, args);

        Map<Long, Film> filmMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Film film = (Film) row[0];
            Genre genre = (Genre) row[1];
            Mpa mpa = (Mpa) row[2];
            film.setMpa(mpa);

            filmMap.computeIfAbsent(film.getId(), k -> {
                film.setGenres(new LinkedHashSet<>()); // Set
                return film;
            });
            if (genre != null) {
                filmMap.get(film.getId()).getGenres().add(genre);
            }
        }

        loadDirectorsFor(filmMap);

        // вернуть в исходном порядке ids
        List<Film> ordered = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Film f = filmMap.get(id);
            if (f != null) ordered.add(f);
        }
        return ordered;
    }

    public Collection<Film> findByDirectorSorted(Long directorId, String sortBy) {
        List<Long> ids = switch (sortBy) {
            case "year" -> findFilmIdsByDirectorOrderByYear(directorId);
            case "likes" -> findFilmIdsByDirectorOrderByLikes(directorId);
            default -> throw new IllegalArgumentException("Sort must be 'year' or 'likes'");
        };
        return findRichByIdsPreservingOrder(ids);
    }
}
