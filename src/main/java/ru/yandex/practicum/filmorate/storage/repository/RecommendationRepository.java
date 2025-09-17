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
                LIMIT 10
                """;
            return jdbcTemplate.queryForList(sql, Long.class, userId);
        } catch (Exception e) {
            // Если запрос не работает, попробуем получить все фильмы
            try {
                String fallbackSql = "SELECT id FROM film ORDER BY id LIMIT 10";
                return jdbcTemplate.queryForList(fallbackSql, Long.class);
            } catch (Exception ex) {
                return new ArrayList<>();
            }
        }
    }
}