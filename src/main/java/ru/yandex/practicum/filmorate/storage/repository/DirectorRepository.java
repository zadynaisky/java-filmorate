package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Repository
@Slf4j
public class DirectorRepository extends BaseRepository<Director> {
    private final DirectorRowMapper directorRowMapper;

    public DirectorRepository(JdbcTemplate jdbcTemplate, DirectorRowMapper directorRowMapper) {
        super(jdbcTemplate);
        this.directorRowMapper = directorRowMapper;
    }

    public Collection<Director> findAll() {
        String sql = "SELECT * FROM director ORDER BY name;";
        return findMany(sql, directorRowMapper);
    }

    public Director findById(long id) {
        String sql = "SELECT * FROM director WHERE id = ?;";
        return findOne(sql, directorRowMapper, id);
    }

    public Set<Director> findByFilmId(long filmId) {
        String sql = """
                SELECT d.id, d.name
                FROM film_director as fd
                INNER JOIN director as d ON fd.director_id = d.id
                WHERE fd.film_id = ?
                ORDER BY d.name;
                """;
        return new HashSet<>(findMany(sql, directorRowMapper, filmId));
    }

    public Director create(Director director) {
        String sql = "INSERT INTO director (name) VALUES (?);";
        long id = insert(sql, director.getName());
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        String sql = "UPDATE director SET name = ? WHERE id = ?;";
        update(sql, director.getName(), director.getId());
        return director;
    }

    public void delete(long id) {
        String sql = "DELETE FROM director WHERE id = ?;";
        delete(sql, id);
    }
}