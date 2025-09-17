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

    public Set<Long> getUserLikedFilms(Long userId) {
        try {
            String sql = "SELECT film_id FROM \"like\" WHERE user_id = ?";
            List<Long> filmIds = jdbcTemplate.queryForList(sql, Long.class, userId);
            return new HashSet<>(filmIds);
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    public Set<Long> getAllUsersWithLikes() {
        try {
            String sql = "SELECT DISTINCT user_id FROM \"like\"";
            List<Long> userIds = jdbcTemplate.queryForList(sql, Long.class);
            return new HashSet<>(userIds);
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    public List<Long> getAllFilmsNotLikedByUser(Long userId) {
        try {
            String sql = """
                SELECT f.id
                FROM film f
                WHERE f.id NOT IN (
                    SELECT COALESCE(l.film_id, -1) FROM \"like\" l WHERE l.user_id = ?
                )
                ORDER BY f.id
                """;
            return jdbcTemplate.queryForList(sql, Long.class, userId);
        } catch (Exception e) {
            try {
                String fallbackSql = "SELECT id FROM film ORDER BY id";
                return jdbcTemplate.queryForList(fallbackSql, Long.class);
            } catch (Exception ex) {
                return new ArrayList<>();
            }
        }
    }

    /**
     * Найти пользователя с максимальным количеством общих лайков
     */
    public Long findUserWithMostCommonLikes(Long userId) {
        try {
            String sql = """
                SELECT l2.user_id
                FROM \"like\" l1
                JOIN \"like\" l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ? AND l2.user_id != ?
                GROUP BY l2.user_id
                HAVING COUNT(*) > 0
                ORDER BY COUNT(*) DESC
                """;

            List<Long> users = jdbcTemplate.queryForList(sql, Long.class, userId, userId);
            return users.isEmpty() ? null : users.get(0);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Получить ID фильмов, которые лайкнул targetUserId, но не лайкнул currentUserId
     */
    public List<Long> getRecommendedFilmIds(Long currentUserId, Long similarUserId) {
        // Лайки текущего пользователя
        Set<Long> currentUserLikes = getUserLikedFilms(currentUserId);

        // Лайки похожего пользователя
        Set<Long> similarUserLikes = getUserLikedFilms(similarUserId);

        // Убираем те фильмы, которые уже есть у текущего
        similarUserLikes.removeAll(currentUserLikes);

        // Возвращаем список, отсортированный по id
        return similarUserLikes.stream()
                .sorted()
                .toList();
    }

}