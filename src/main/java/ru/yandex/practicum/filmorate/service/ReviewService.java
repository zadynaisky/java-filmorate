package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.repository.ReviewRepository;

import java.time.Instant;
import java.util.Collection;

import static ru.yandex.practicum.filmorate.model.EventType.REVIEW;
import static ru.yandex.practicum.filmorate.model.OperationType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final EventService eventService;

    public Review create(Review review) {
        validateUser(review.getUserId());
        validateFilm(review.getFilmId());
        review = reviewRepository.create(review);

        eventService.create(new Event(Instant.now().toEpochMilli(), REVIEW, ADD, review.getUserId(), review.getId()));

        return review;
    }

    public Review update(Review review) {
        validateUser(review.getUserId());
        validateFilm(review.getFilmId());
        review = reviewRepository.update(review);
        eventService.create(new Event(Instant.now().toEpochMilli(), REVIEW, UPDATE, review.getUserId(), review.getId()));
        return review;
    }

    public boolean delete(long id) {
        var review = reviewRepository.findById(id);
        var result = reviewRepository.delete(id);
        eventService.create(new Event(Instant.now().toEpochMilli(), REVIEW, REMOVE, review.getUserId(), review.getId()));
        return result;
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
