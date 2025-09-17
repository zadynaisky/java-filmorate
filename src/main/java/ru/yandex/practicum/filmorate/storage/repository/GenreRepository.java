package ru.yandex.practicum.filmorate.storage.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.Collection;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private final GenreRowMapper genreRowMapper;

    public GenreRepository(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        super(jdbcTemplate);
        this.genreRowMapper = genreRowMapper;
    }

    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM genre;";
        return findMany(sql, genreRowMapper);
    }

    public Genre findById(long id) {
        String sql = "SELECT * FROM genre WHERE id = ?;";
        return findOne(sql, genreRowMapper, id);
    }

    public Collection<Genre> findByFilmId(long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM film_genre as fg
                INNER JOIN genre as g ON fg.genre_id = g.id
                WHERE fg.film_id = ?;
                """;
        return findMany(sql, genreRowMapper, filmId);
    }
}
