package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.repository.ReviewRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public Review create(Review review) {
        validateUser(review.getUserId());
        validateFilm(review.getFilmId());
        return reviewRepository.create(review);
    }

    public Review update(Review review) {
        validateUser(review.getUserId());
        validateFilm(review.getFilmId());
        return reviewRepository.update(review);
    }

    public boolean delete(long id) {
        return reviewRepository.delete(id);
    }

    public Collection<Review> findAll(Long filmId, int count) {
        if (filmId == null)
            return reviewRepository.findAll(count);

        return reviewRepository.findAllByFilmId(filmId, count);
    }

    public Review findById(Long id) {
        var review = reviewRepository.findById(id);
        if (review == null)
            throw new NotFoundException("Review not found");
        return review;
    }

    public void addLike(long reviewId, long userId) {
        validateUser(userId);
        reviewRepository.addLikeOrDislike(reviewId, userId, true);
    }

    public void addDislike(long reviewId, long userId) {
        validateUser(userId);
        reviewRepository.addLikeOrDislike(reviewId, userId, false);
    }

    public void removeLike(long reviewId, long userId) {
        validateUser(userId);
        reviewRepository.removeLikeOrDislike(reviewId, userId);
    }

    public void removeDislike(long reviewId, long userId) {
        validateUser(userId);
        reviewRepository.removeLikeOrDislike(reviewId, userId);
    }

    public void validateUser(Long userId) {
        if (userId < 1)
            throw new NotFoundException("User not found");
    }

    public void validateFilm(Long filmId) {
        if (filmId < 1)
            throw new NotFoundException("Film not found");
    }
}
