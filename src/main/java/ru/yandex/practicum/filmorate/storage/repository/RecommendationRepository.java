package ru.yandex.practicum.filmorate.storage.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Repository
public class RecommendationRepository extends BaseRepository<Film> {

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    /**
     * Получить список фильмов, которые лайкнул пользователь
     */
    public Set<Long> getUserLikedFilms(Long userId) {
        String sql = "SELECT film_id FROM \"like\" WHERE user_id = ?";
        List<Long> filmIds = jdbcTemplate.queryForList(sql, Long.class, userId);
        return new HashSet<>(filmIds);
    }

    /**
     * Получить всех пользователей, которые поставили лайки
     */
    public Set<Long> getAllUsersWithLikes() {
        String sql = "SELECT DISTINCT user_id FROM \"like\"";
        List<Long> userIds = jdbcTemplate.queryForList(sql, Long.class);
        return new HashSet<>(userIds);
    }

    /**
     * Получить количество общих лайков между двумя пользователями
     */
    public int getCommonLikesCount(Long userId1, Long userId2) {
        String sql = """
            SELECT COUNT(*) FROM \"like\" l1
            JOIN \"like\" l2 ON l1.film_id = l2.film_id
            WHERE l1.user_id = ? AND l2.user_id = ?
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId1, userId2);
        return count != null ? count : 0;
    }

    /**
     * Получить ID фильмов, которые лайкнул targetUserId, но не лайкнул currentUserId
     */
    public List<Long> getRecommendedFilmIds(Long currentUserId, Long targetUserId) {
        String sql = """
            SELECT DISTINCT l.film_id
            FROM \"like\" l
            WHERE l.user_id = ?
            AND l.film_id NOT IN (
                SELECT COALESCE(film_id, -1) FROM \"like\" WHERE user_id = ?
            )
            ORDER BY l.film_id
            """;

        return jdbcTemplate.queryForList(sql, Long.class, targetUserId, currentUserId);
    }

    /**
     * Найти пользователя с максимальным количеством общих лайков
     * Если нет общих лайков, найти любого пользователя с лайками
     */
    public Long findUserWithMostCommonLikes(Long userId) {
        try {
            // Сначала попробуем найти пользователей с общими лайками
            String sql = """
                SELECT l2.user_id, COUNT(*) as common_count
                FROM \"like\" l1
                JOIN \"like\" l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ? AND l2.user_id != ?
                GROUP BY l2.user_id
                ORDER BY common_count DESC
                LIMIT 1
                """;

            List<Long> users = jdbcTemplate.queryForList(sql, Long.class, userId, userId);
            if (!users.isEmpty()) {
                return users.get(0);
            }

            // Если нет общих лайков, найдем любого пользователя, который хотя бы что-то лайкнул
            String fallbackSql = """
                SELECT DISTINCT user_id
                FROM \"like\"
                WHERE user_id != ?
                ORDER BY user_id
                LIMIT 1
                """;

            List<Long> fallbackUsers = jdbcTemplate.queryForList(fallbackSql, Long.class, userId);
            return fallbackUsers.isEmpty() ? null : fallbackUsers.get(0);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Получить все фильмы, которые не лайкнул пользователь
     */
    public List<Long> getAllFilmsNotLikedByUser(Long userId) {
        String sql = """
            SELECT DISTINCT f.id
            FROM film f
            WHERE f.id NOT IN (
                SELECT COALESCE(film_id, -1) FROM \"like\" WHERE user_id = ?
            )
            ORDER BY f.id
            LIMIT 10
            """;

        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }
}