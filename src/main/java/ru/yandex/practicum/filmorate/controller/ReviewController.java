package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public Review findReview(@PathVariable("id") long reviewId) {
        return reviewService.findById(reviewId);
    }

    @GetMapping
    public Collection<Review> findAll(@RequestParam(name = "count", defaultValue = "10") int count,
                                      @RequestParam(name = "filmId", required = false) Long filmId) {
        return reviewService.findAll(filmId, count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review newReview) {
        return reviewService.update(newReview);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long reviewId) {
        reviewService.delete(reviewId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") long reviewId, @PathVariable("userId") long userId) {
        reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") long reviewId, @PathVariable("userId") long userId) {
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") long reviewId, @PathVariable("userId") long userId) {
        reviewService.removeLike(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable("id") long reviewId, @PathVariable("userId") long userId) {
        reviewService.removeDislike(reviewId, userId);
    }
}
