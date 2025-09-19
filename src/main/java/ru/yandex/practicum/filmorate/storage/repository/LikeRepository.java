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

    /**
     * Получить список фильмов, которые лайкнул пользователь
     */
    public Collection<Long> getLikedFilmsByUser(final Long userId) {
        String sql = "SELECT film_id FROM \"LIKE\" WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    /**
     * Получить список пользователей, которые лайкнули фильм
     */
    public Collection<Long> getUsersWhoLikedFilm(final Long filmId) {
        String sql = "SELECT user_id FROM \"LIKE\" WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, filmId);
    }

    /**
     * Получить всех пользователей с их лайками (если где-то потребуется)
     */
    public Map<Long, Set<Long>> getAllUsersLikes() {
        String sql = "SELECT user_id, film_id FROM \"LIKE\"";
        Map<Long, Set<Long>> userLikes = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long userId = rs.getLong("user_id");
            Long filmId = rs.getLong("film_id");
            userLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        });

        return userLikes;
    }
}
