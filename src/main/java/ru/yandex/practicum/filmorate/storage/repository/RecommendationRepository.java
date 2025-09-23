package ru.yandex.practicum.filmorate.storage.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
public class RecommendationRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** ID фильмов, которые лайкнул пользователь */
    public Set<Long> getUserLikedFilms(Long userId) {
        String sql = "SELECT film_id FROM \"LIKE\" WHERE user_id = ?";
        // Сохраняем порядок и убираем дубли
        return new LinkedHashSet<>(jdbcTemplate.query(sql,
                (rs, rn) -> rs.getLong("film_id"),
                userId));
    }

    /** Наиболее похожий пользователь по числу общих лайков (или null, если нет) */
    public Long findUserWithMostCommonLikes(Long userId) {
        String sql = """
            SELECT l2.user_id AS similar_user_id, COUNT(*) AS c
            FROM "LIKE" l1
            JOIN "LIKE" l2 ON l1.film_id = l2.film_id
            WHERE l1.user_id = ? AND l2.user_id <> ?
            GROUP BY l2.user_id
            ORDER BY c DESC, l2.user_id
            LIMIT 1
        """;
        List<Long> ids = jdbcTemplate.query(sql,
                (rs, rn) -> rs.getLong("similar_user_id"),
                userId, userId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    /** Список похожих пользователей, отсортированный по числу общих лайков (может быть пустым) */
    public List<Long> findUsersWithCommonLikes(Long userId) {
        String sql = """
            SELECT l2.user_id AS similar_user_id, COUNT(*) AS c
            FROM "LIKE" l1
            JOIN "LIKE" l2 ON l1.film_id = l2.film_id
            WHERE l1.user_id = ? AND l2.user_id <> ?
            GROUP BY l2.user_id
            ORDER BY c DESC, l2.user_id
        """;
        return jdbcTemplate.query(sql,
                (rs, rn) -> rs.getLong("similar_user_id"),
                userId, userId);
    }

    /** Фильмы, которые лайкнул похожий пользователь, но не лайкнул текущий (в порядке популярности) */
    public List<Long> getRecommendedFilmIds(Long userId, Long similarUserId) {
        String sql = """
            SELECT l2.film_id
            FROM "LIKE" l2
            WHERE l2.user_id = ?
              AND l2.film_id NOT IN (SELECT film_id FROM "LIKE" WHERE user_id = ?)
            ORDER BY (
                SELECT COUNT(*) FROM "LIKE" lx WHERE lx.film_id = l2.film_id
            ) DESC, l2.film_id
        """;
        return jdbcTemplate.query(sql,
                (rs, rn) -> rs.getLong("film_id"),
                similarUserId, userId);
    }
}
