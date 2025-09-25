package ru.yandex.practicum.filmorate.storage.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private final GenreRowMapper genreRowMapper;

    public GenreRepository(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        super(jdbcTemplate);
        this.genreRowMapper = genreRowMapper;
    }

    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genre ORDER BY id";
        return new ArrayList<>(findMany(sql, genreRowMapper));
    }

    public Genre findById(long id) {
        String sql = "SELECT id, name FROM genre WHERE id = ?";
        return findOne(sql, genreRowMapper, id);
    }

    public List<Genre> findByFilmId(long filmId) {
        String sql = """
                SELECT DISTINCT g.id, g.name
                FROM film_genre fg
                JOIN genre g ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;
        return new ArrayList<>(findMany(sql, genreRowMapper, filmId));
    }
}
