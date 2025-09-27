package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Validator validator;
    private Film film = new Film();

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
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

    }

    @Test
    void shouldCreateValidFilm() {
        assertTrue(validator.validate(film).isEmpty());
        assertNotNull(film);
        assertEquals("The Matrix", film.getName());
        assertEquals("A mind-bending science fiction film", film.getDescription());
        assertEquals(LocalDate.of(1999, 3, 31), film.getReleaseDate());
        assertEquals(136, film.getDuration());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        film.setName(" ");
        assertEquals(1, validator.validate(film).size());
        film.setName("  ");
        assertEquals(1, validator.validate(film).size());
    }

    @Test
    void shouldFailWhenReleaseDateBefore1895_12_28() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertEquals(1, validator.validate(film).size());
    }

    @Test
    void shouldCreateValidFilmWhenReleaseDateIs1895_12_28() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void shouldCreateValidFilmWhenReleaseDateIsToday() {
        film.setReleaseDate(LocalDate.now());
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void shouldAllowFutureReleaseDate() {
        film.setReleaseDate(LocalDate.now().plusDays(1));
        assertEquals(0, validator.validate(film).size());
    }

    @Test
    void shouldFailWhenBeforeCinemaBirthday() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertEquals(1, validator.validate(film).size());
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        film.setDuration(-14);
        assertEquals(1, validator.validate(film).size());
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        film.setDuration(0);
        assertEquals(1, validator.validate(film).size());
    }

    @Test
    void testEqualsAndHashCode() {
        Film film1 = new Film();
        film1.setName("Inception");
        film1.setDescription("A dream within a dream");
        film1.setReleaseDate(LocalDate.of(2010, 7, 16));
        film1.setDuration(148);

        Film film2 = new Film();
        film2.setName("Inception");
        film2.setDescription("A dream within a dream");
        film2.setReleaseDate(LocalDate.of(2010, 7, 16));
        film2.setDuration(148);

        assertTrue(film1.equals(film2));
        assertEquals(film1.hashCode(), film2.hashCode());
    }
}