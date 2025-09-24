package ru.yandex.practicum.filmorate.storage.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
public class RecommendationRepository extends BaseRepository<Film> {

    private static final Logger log = LoggerFactory.getLogger(RecommendationRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Наиболее похожий пользователь по числу общих лайков (или null, если нет).
     */
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

    /**
     * Пользователи с общими лайками, отсортированные по количеству общих лайков (по убыванию).
     */
    public List<Long> findUsersWithCommonLikes(Long userId) {
        String sql = """
                    SELECT l2.user_id
                    FROM "LIKE" l1
                    JOIN "LIKE" l2 ON l1.film_id = l2.film_id
                    WHERE l1.user_id = ? AND l2.user_id <> ?
                    GROUP BY l2.user_id
                    HAVING COUNT(*) > 0
                    ORDER BY COUNT(*) DESC
                """;
        try {
            return jdbcTemplate.queryForList(sql, Long.class, userId, userId);
        } catch (Exception e) {
            log.error("Error finding users with common likes for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Фильмы, которые лайкнул похожий пользователь, но не лайкнул текущий (в порядке популярности).
     */
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
