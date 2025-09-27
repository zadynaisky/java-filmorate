package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper2;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper2;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
        var list = findRichByIdsPreservingOrder(List.of(id));
        if (list.isEmpty()) throw new NotFoundException("Film not found: " + id);
        Film f = list.iterator().next();
        normalizeFilm(f);
        return f;
    }

    @Override
    public Collection<Film> findAll() {
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
        filmMap.values().forEach(this::normalizeFilm);

        var films = new ArrayList<>(filmMap.values());
        films.forEach(this::normalizeFilm);
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

        Film saved = findRichByIdOrThrow(id);
        normalizeFilm(saved);
        return saved;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?,?);";
        List<Genre> genres = film.getGenres().stream()
                .filter(g -> g != null && g.getId() != null && g.getId() > 0)
                .toList();

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
        updateDirectors(film);

        Film saved = findRichByIdOrThrow(film.getId());
        normalizeFilm(saved);
        return saved;
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return; // жанры очищены
        }

        List<Long> genreIds = film.getGenres().stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();

        if (genreIds.isEmpty()) return;

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genreIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return genreIds.size();
                    }
                }
        );
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
                        ORDER BY c DESC, f1.id
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
        filmMap.values().forEach(this::normalizeFilm);
        return filmMap.values();
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
                ORDER BY f1.c DESC, f2.id;
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
        filmMap.values().forEach(this::normalizeFilm);
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
        if (film.getDirectors() == null) {
            return;
        }
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
            if (f != null) {
                normalizeFilm(f);
                ordered.add(f);
            }
        }
        return ordered;
    }

    public Collection<Film> findByDirectorSorted(Long directorId, SortBy sortBy) {
        List<Long> ids = switch (sortBy) {
            case YEAR -> findFilmIdsByDirectorOrderByYear(directorId);
            case LIKES -> findFilmIdsByDirectorOrderByLikes(directorId);
        };
        return findRichByIdsPreservingOrder(ids);
    }

    public Collection<Film> search(String query, boolean byTitle, boolean byDirector) {
        if (!byTitle && !byDirector) {
            return List.of();
        }

        final String pattern = "%" + query.trim().toLowerCase() + "%";
        final String sqlTitle = """
                SELECT f.id AS id
                FROM film f
                LEFT JOIN "LIKE" l on l.film_id = f.id
                WHERE LOWER(f.name) LIKE ?
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC, f.id ASC
                """;

        final String sqlDirector = """
                SELECT f.id AS id
                FROM film f
                JOIN film_director fd ON fd.film_id = f.id
                JOIN director d ON d.id = fd.director_id
                LEFT JOIN "LIKE" l ON l.film_id = f.id
                WHERE LOWER(d.name) LIKE ?
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC, f.id ASC
                """;

        final String sqlBoth = """
                WITH ids AS (
                     SELECT f.id
                     FROM film f
                     WHERE LOWER(f.name) LIKE ?
                     UNION
                     SELECT f2.id
                     FROM film f2
                     JOIN film_director fd ON fd.film_id = f2.id
                     JOIN director d ON d.id = fd.director_id
                     WHERE LOWER(d.name) LIKE ?
                )
                SELECT f.id AS id
                FROM ids
                JOIN film f ON f.id = ids.id
                LEFT JOIN "LIKE" l ON l.film_id = f.id
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC, f.id ASC
                """;

        List<Long> ids;
        if (byTitle && byDirector) {
            ids = jdbcTemplate.query(sqlBoth, (rs, rn) -> rs.getLong("id"), pattern, pattern);
        } else if (byTitle) {
            ids = jdbcTemplate.query(sqlTitle, (rs, rn) -> rs.getLong("id"), pattern);
        } else {
            ids = jdbcTemplate.query(sqlDirector, (rs, rn) -> rs.getLong("id"), pattern);
        }
        return findRichByIdsPreservingOrder(ids);
    }

    private Film findRichByIdOrThrow(Long id) {
        var list = findRichByIdsPreservingOrder(List.of(id));
        if (list.isEmpty()) throw new NotFoundException("Film not found: " + id);
        return list.iterator().next();
    }

    private void normalizeFilm(Film f) {
        if (f == null) return;
        if (f.getGenres() != null) {
            f.setGenres(f.getGenres().stream()
                    .sorted(Comparator.comparingLong(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        if (f.getDirectors() != null) {
            f.setDirectors(f.getDirectors().stream()
                    .sorted(Comparator.comparingLong(Director::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
    }

    public List<Film> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM film WHERE id IN (" + placeholders + ")";

        return jdbcTemplate.query(sql, filmRowMapper, ids.toArray());
    }
}
