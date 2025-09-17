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

    // Получить всех пользователей, которые поставили лайки
    public Set<Long> getAllUsersWithLikes() {
        try {
            String sql = "SELECT DISTINCT user_id FROM \"like\"";
            List<Long> userIds = jdbcTemplate.queryForList(sql, Long.class);
            return new HashSet<>(userIds);
        } catch (Exception e) {
            log.error("Error getting all users with likes: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    // Найти пользователя с максимальным количеством общих лайков
    public Long findUserWithMostCommonLikes(Long userId) {
        try {
            String sql = """
                SELECT l2.user_id
                FROM "like" l1
                JOIN "like" l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ? AND l2.user_id != ?
                GROUP BY l2.user_id
                HAVING COUNT(*) > 0
                ORDER BY COUNT(*) DESC
                LIMIT 1
                """;
            List<Long> users = jdbcTemplate.queryForList(sql, Long.class, userId, userId);
            return users.isEmpty() ? null : users.get(0);
        } catch (Exception e) {
            log.error("Error finding similar user for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    // Получить рекомендации: фильмы, которые лайкнул похожий пользователь, но не текущий
    public List<Long> getRecommendedFilmIds(Long currentUserId, Long similarUserId) {
        try {
            String sql = """
                SELECT DISTINCT l.film_id
                FROM \"like\" l
                WHERE l.user_id = ?
                AND l.film_id NOT IN (
                    SELECT COALESCE(film_id, -1) FROM \"like\" WHERE user_id = ?
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

    // Получить любые фильмы, которые пользователь еще не лайкал
    public List<Long> getAnyUnlikedFilmIds(Long userId, Set<Long> userLikedFilms, int limit) {
        try {
            String sql = """
                SELECT id FROM film 
                WHERE id NOT IN (
                    SELECT COALESCE(film_id, -1) FROM \"like\" WHERE user_id = ?
                )
                ORDER BY id
                LIMIT ?
                """;

            return jdbcTemplate.queryForList(sql, Long.class, userId, limit);
        } catch (Exception e) {
            log.error("Error getting unliked films for user {}: {}", userId, e.getMessage());

            // Fallback: возвращаем все фильмы, исключая лайкнутые
            try {
                String allFilmsSql = "SELECT id FROM film ORDER BY id";
                List<Long> allFilmIds = jdbcTemplate.queryForList(allFilmsSql, Long.class);

                List<Long> result = new ArrayList<>();
                for (Long filmId : allFilmIds) {
                    if (!userLikedFilms.contains(filmId) && result.size() < limit) {
                        result.add(filmId);
                    }
                }
                return result;
            } catch (Exception ex) {
                log.error("Error in fallback for getting unliked films: {}", ex.getMessage());
                return Collections.emptyList();
            }
        }
    }
}