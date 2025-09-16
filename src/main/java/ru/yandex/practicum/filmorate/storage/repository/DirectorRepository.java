package ru.yandex.practicum.filmorate.storage.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DirectorRepository implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO director (name) VALUES (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKeyAs(Long.class));
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE director SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteDirector(Long id) {
        String sql = "DELETE FROM director WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Director> getDirectorById(Long id) {
        String sql = "SELECT * FROM director WHERE id = ?";
        try {
            Director director = jdbcTemplate.queryForObject(sql, directorRowMapper, id);
            return Optional.ofNullable(director);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM director ORDER BY id";
        return jdbcTemplate.query(sql, directorRowMapper);
    }

    @Override
    public boolean exists(Long id) {
        String sql = "SELECT COUNT(*) FROM director WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Director> getDirectorsByFilmId(Long filmId) {
        String sql = """
            SELECT d.* FROM director d
            INNER JOIN film_director fd ON d.id = fd.director_id
            WHERE fd.film_id = ?
            ORDER BY d.id
            """;
        return jdbcTemplate.query(sql, directorRowMapper, filmId);
    }

    @Override
    public void addDirectorToFilm(Long filmId, Long directorId) {
        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, directorId);
    }

    @Override
    public void removeDirectorsFromFilm(Long filmId) {
        String sql = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}