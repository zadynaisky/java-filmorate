package ru.yandex.practicum.filmorate.storage.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Repository
public class RecommendationRepository extends BaseRepository<Film> {

    private static final Logger log = LoggerFactory.getLogger(RecommendationRepository.class);

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // Получить все лайки пользователя
    public Set<Long> getUserLikedFilms(Long userId) {
        try {
            String sql = "SELECT film_id FROM \"like\" WHERE user_id = ?";
            List<Long> filmIds = jdbcTemplate.queryForList(sql, Long.class, userId);
            return new HashSet<>(filmIds);
        } catch (Exception e) {
            log.error("Error getting user liked films for user {}: {}", userId, e.getMessage());
            return new HashSet<>();
        }
    }

    // Найти пользователей с общими лайками, отсортированных по количеству общих лайков
    public List<Long> findUsersWithCommonLikes(Long userId) {
        try {
            String sql = """
                SELECT l2.user_id
                FROM "like" l1
                JOIN "like" l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ? AND l2.user_id != ?
                GROUP BY l2.user_id
                HAVING COUNT(*) > 0
                ORDER BY COUNT(*) DESC
                """;
            return jdbcTemplate.queryForList(sql, Long.class, userId, userId);
        } catch (Exception e) {
            log.error("Error finding users with common likes for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // Получить рекомендации: фильмы, которые лайкнул похожий пользователь, но не текущий
    public List<Long> getRecommendedFilmIds(Long currentUserId, Long similarUserId) {
        try {
            String sql = """
                SELECT l.film_id
                FROM \"like\" l
                WHERE l.user_id = ?
                AND l.film_id NOT IN (
                    SELECT film_id FROM \"like\" WHERE user_id = ?
                )
                ORDER BY l.film_id
                """;

            return jdbcTemplate.queryForList(sql, Long.class, similarUserId, currentUserId);
        } catch (Exception e) {
            log.error("Error getting recommended films from user {} for user {}: {}",
                    similarUserId, currentUserId, e.getMessage());

            // Fallback: используем Set операции
            Set<Long> currentUserLikes = getUserLikedFilms(currentUserId);
            Set<Long> similarUserLikes = getUserLikedFilms(similarUserId);

            Set<Long> recommended = new HashSet<>(similarUserLikes);
            recommended.removeAll(currentUserLikes);

            return new ArrayList<>(recommended);
        }
    }

    // Альтернативный метод: найти любые фильмы, которые не лайкнул пользователь
    public List<Long> findAnyUnlikedFilms(Long userId, int limit) {
        try {
            String sql = """
                SELECT f.id 
                FROM film f
                WHERE f.id NOT IN (
                    SELECT film_id FROM \"like\" WHERE user_id = ?
                )
                ORDER BY f.id
                LIMIT ?
                """;
            return jdbcTemplate.queryForList(sql, Long.class, userId, limit);
        } catch (Exception e) {
            log.error("Error finding any unliked films for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }
}