package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.*;
import ru.yandex.practicum.filmorate.storage.repository.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewRepository.class, ReviewRowMapper.class, UserRepository.class, UserRowMapper.class,
        FilmRepository.class, FilmRowMapper.class, UserRepository.class, UserRowMapper.class,
        MpaRepository.class, MpaRowMapper.class, GenreRepository.class, GenreRowMapper.class})
public class ReviewRepositoryTest {
    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    private Film film;
    private User user;


    @BeforeEach
    void setUp() {
        film = new Film();
        film.setName("The Matrix");
        film.setDescription("A mind-bending science fiction film");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("PG");
        mpa.setDescription("Some description");
        film.setMpa(mpa);
        filmRepository.create(film);

        user = new User();
        user.setBirthday(LocalDate.of(1999, 3, 31));
        user.setName("Leonardo");
        user.setEmail("leonardo@yandex.ru");
        user.setLogin("leonardo");
        user = userRepository.create(user);
    }

    @Test
    void testCreateReview() {
        Review review = new Review();
        review.setContent("some content about movie");
        review.setFilmId(film.getId());
        review.setUserId(user.getId());
        review.setIsPositive(true);

        Review createdReview = reviewRepository.create(review);
        assertNotNull(createdReview);
        assertTrue(createdReview.getIsPositive());
        assertEquals("some content about movie", createdReview.getContent());
        assertEquals(film.getId(), createdReview.getFilmId());
        assertEquals(user.getId(), createdReview.getUserId());
    }
}
