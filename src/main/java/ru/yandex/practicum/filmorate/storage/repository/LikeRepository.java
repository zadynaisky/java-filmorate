package ru.yandex.practicum.filmorate.storage.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LikeRepository extends BaseRepository<Long> {
    public LikeRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void addLike(final Long userId, final Long filmId) {
        String sql = "INSERT INTO \"LIKE\" (user_id, film_id) VALUES (?, ?)";
        insertMultipleKeys(sql, userId, filmId);
    }

    public void removeLike(final Long userId, final Long filmId) {
        String sql = "DELETE FROM \"LIKE\" WHERE user_id = ? AND film_id = ?";
        delete(sql, userId, filmId);
    }
}
