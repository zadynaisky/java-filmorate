package ru.yandex.practicum.filmorate.storage.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;

import java.util.Collection;

@Repository
public class DirectorRepository extends BaseRepository<Director> {
    private final DirectorRowMapper directorRowMapper = new DirectorRowMapper();

    public DirectorRepository (JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public Director create(Director director) {
        String sql = "INSERT INTO DIRECTOR (name) VALUES (?);";
        Long id = insert(sql, director.getName());
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        if (director.getId() == null) {
            throw new NotFoundException("Director id is required");
        }

        String sql = "UPDATE DIRECTOR SET name = ? WHERE id = ?;";
        update(sql, director.getName(), director.getId());
        return findById(director.getId());
    }

    public Director findById(Long id) {
        String sql = "SELECT id, name FROM DIRECTOR WHERE id = ?;";
        Director director = findOne(sql, directorRowMapper, id);
        if (director == null) {
            throw new NotFoundException("Director not found: " + id);
        }
        return director;
    }

    public Collection<Director> findAll() {
        String sql = "SELECT id, name FROM DIRECTOR ORDER BY id;";
        return findMany(sql, directorRowMapper);
    }

    public void deleteById (Long id) {
        String sql = "DELETE FROM DIRECTOR WHERE id = ?;";
        boolean isDeleted = delete(sql, id);
        if (!isDeleted) throw new NotFoundException("Director not found: " + id);
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM DIRECTOR WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
