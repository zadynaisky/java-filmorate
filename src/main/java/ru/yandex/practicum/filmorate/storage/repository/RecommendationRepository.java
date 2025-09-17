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

    // Получить все лайки пользователя
    public Set<Long> getUserLikedFilms(Long userId) {
        try {
            String sql = "SELECT film_id FROM \"like\" WHERE user_id = ?";
            List<Long> filmIds = jdbcTemplate.queryForList(sql, Long.class, userId);
            return new HashSet<>(filmIds);
        } catch (Exception e) {
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
                ORDER BY COUNT(*) DESC
                """;
            List<Long> users = jdbcTemplate.queryForList(sql, Long.class, userId, userId);
            return users.isEmpty() ? null : users.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    // Получить рекомендации: фильмы, которые лайкнул похожий пользователь, но не текущий
    public List<Long> getRecommendedFilmIds(Long currentUserId, Long similarUserId) {
        Set<Long> currentUserLikes = getUserLikedFilms(currentUserId);
        Set<Long> similarUserLikes = getUserLikedFilms(similarUserId);

        Set<Long> recommended = new HashSet<>(similarUserLikes);
        recommended.removeAll(currentUserLikes);

        return recommended.stream()
                .sorted()
                .toList();
    }
}
