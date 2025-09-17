package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRowMapper;

import java.util.Collection;

@Repository
@Slf4j
public class ReviewRepository extends BaseRepository<Review> {
    private final ReviewRowMapper reviewRowMapper;

    public ReviewRepository(JdbcTemplate jdbcTemplate, ReviewRowMapper reviewRowMapper) {
        super(jdbcTemplate);
        this.reviewRowMapper = reviewRowMapper;
    }

    public Collection<Review> findAll(int count) {
        String sql = "SELECT * FROM \"REVIEW\" ORDER BY useful DESC LIMIT ?;";
        return findMany(sql, reviewRowMapper, count);
    }

    public Collection<Review> findAllByFilmId(long filmId, int count) {
        String sql = "SELECT * FROM \"REVIEW\" WHERE film_id = ? ORDER BY useful DESC LIMIT ?;";
        return findMany(sql, reviewRowMapper, filmId, count);
    }

    public Review findById(long id) {
        String sql = "SELECT * FROM \"REVIEW\" WHERE id = ?;";
        return findOne(sql, reviewRowMapper, id);
    }

    public Review create(Review review) {
        String sql = "INSERT INTO \"REVIEW\" (content, is_positive, film_id, user_id) VALUES (?, ?, ?, ?);";
        var id = insert(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getFilmId(),
                review.getUserId());
        review.setId(id);
        return review;
    }

    public Review update(Review review) {
        String sql = "UPDATE \"REVIEW\" SET content = ?, is_positive = ?, film_id = ?, user_id = ? WHERE id = ?;";
        update(sql, review.getContent(), review.getIsPositive(), review.getFilmId(), review.getUserId(), review.getId());
        return review;
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM \"REVIEW\" WHERE id = ?;";
        return delete(sql, id);
    }

    public void addLikeOrDislike(final Long reviewId, final Long userId, final boolean isLike) {
        String sql = "MERGE INTO \"REVIEW_RATING\" (review_id, user_id, is_positive) VALUES (?, ?, ?)";
        log.info("Adding like or dislike to review {} and user {}", reviewId, userId);
        insertMultipleKeys(sql, reviewId, userId, isLike);
        updateUseful(reviewId);
    }

    public void removeLikeOrDislike(final Long reviewId, final Long userId) {
        String sql = "DELETE FROM \"REVIEW_RATING\" WHERE review_id = ? AND user_id = ?";
        log.info("Removing like  or dislike from review {} and user {}", reviewId, userId);
        delete(sql, reviewId, userId);
        updateUseful(reviewId);
    }

    public void updateUseful(long reviewId) {
        String sql = """
                UPDATE \"REVIEW\"
                SET useful =
                        (SELECT SUM(CASE
                                WHEN is_positive = TRUE THEN 1
                                WHEN is_positive = FALSE THEN -1
                                ELSE 0
                                END)
                        FROM \"REVIEW_RATING\"
                        WHERE review_id = ?)
                    WHERE id = ?;
                """;
        update(sql, reviewId, reviewId);
    }
}
