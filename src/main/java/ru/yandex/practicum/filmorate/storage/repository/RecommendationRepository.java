package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class RecommendationRepository extends BaseRepository<Film> {

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
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
                    ORDER BY COUNT(*) DESC, l2.user_id
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
