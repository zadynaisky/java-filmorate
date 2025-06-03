package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    @Test
    void testValidFilmCreation() {
        Film film = new Film();
        film.setName("The Matrix");
        film.setDescription("A mind-bending science fiction film");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);

        assertNotNull(film);
        assertEquals("The Matrix", film.getName());
        assertEquals("A mind-bending science fiction film", film.getDescription());
        assertEquals(LocalDate.of(1999, 3, 31), film.getReleaseDate());
        assertEquals(136, film.getDuration());
    }

    @Test
    void testNameValidation() {
        Film film = new Film();

        // Проверка на null
        assertThrows(ConstraintViolationException.class, () -> {
            film.setName(null);
        });

        // Проверка на пустую строку
        assertThrows(ConstraintViolationException.class, () -> {
            film.setName("");
        });
    }

    @Test
    void testDescriptionValidation() {
        Film film = new Film();

        // Проверка на null
        assertThrows(ConstraintViolationException.class, () -> {
            film.setDescription(null);
        });

        // Проверка на пустую строку
        assertThrows(ConstraintViolationException.class, () -> {
            film.setDescription("");
        });

        // Проверка на превышение длины
        assertThrows(ConstraintViolationException.class, () -> {
            film.setDescription("a".repeat(201));
        });
    }

    @Test
    void testReleaseDateValidation() {
        Film film = new Film();

        // Проверка на будущее
        assertThrows(ConstraintViolationException.class, () -> {
            film.setReleaseDate(LocalDate.now().plusDays(1));
        });

        // Проверка на допустимую дату
        film.setReleaseDate(LocalDate.now());
        assertEquals(LocalDate.now(), film.getReleaseDate());
    }

    @Test
    void testDurationValidation() {
        Film film = new Film();

        // Проверка на отрицательное значение
        assertThrows(ConstraintViolationException.class, () -> {
            film.setDuration(-1);
        });

        // Проверка на ноль
        assertThrows(ConstraintViolationException.class, () -> {
            film.setDuration(0);
        });

        // Проверка на положительное значение
        film.setDuration(120);
        assertEquals(120, film.getDuration());
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