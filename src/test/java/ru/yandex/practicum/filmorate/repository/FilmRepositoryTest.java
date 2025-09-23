package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.repository.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmRepository.class, FilmRowMapper.class,
        GenreRepository.class, GenreRowMapper.class,
        MpaRepository.class, MpaRowMapper.class,
        LikeRepository.class,
        UserRepository.class, UserRowMapper.class
})
public class FilmRepositoryTest {

    @Autowired private FilmRepository filmRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private MpaRepository mpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Film film;

    @BeforeEach
    void setup() {
        film = new Film();
        film.setName("One Flew Over the Cuckoo's Nest");
        film.setDescription("One Flew Over the Cuckoo's Nest is a novel by Ken Kesey published in 1962.");
        film.setReleaseDate(LocalDate.parse("1975-10-19"));
        film.setDuration(133);
        film.setMpa(mpaRepository.findById(1));

        Genre g1 = genreRepository.findById(1);
        film.setGenres(new LinkedHashSet<>(Set.of(g1)));  // Set<Genre>, как в модели
    }

    @Test
    void testCreateAndGetById() {
        Film created = filmRepository.create(film);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);

        Film result = filmRepository.findById(created.getId());
        assertEquals(film.getName(), result.getName());
        assertEquals(film.getDescription(), result.getDescription());
        assertEquals(film.getDuration(), result.getDuration());
        assertEquals(film.getReleaseDate(), result.getReleaseDate());
        assertEquals(film.getMpa().getId(), result.getMpa().getId());
    }

    @Test
    void testUpdateFilm() {
        Film created = filmRepository.create(film);

        created.setName("Updated name");
        created.setDescription("Updated description");
        created.setDuration(123);
        created.setReleaseDate(LocalDate.parse("2000-10-19"));
        created.setMpa(mpaRepository.findById(2));

        Genre g2 = genreRepository.findById(2);
        created.setGenres(new LinkedHashSet<>(Set.of(g2)));

        Film updated = filmRepository.update(created);

        assertEquals("Updated name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(123, updated.getDuration());
        assertEquals(LocalDate.parse("2000-10-19"), updated.getReleaseDate());
        assertEquals(2, updated.getMpa().getId());
        assertTrue(updated.getGenres().contains(g2)); // проверяем genres у updated
    }

    @Test
    void testGetAllFilms() {
        filmRepository.create(film);

        Film another = new Film();
        another.setName("Another name");
        another.setDescription("Another description");
        another.setReleaseDate(LocalDate.parse("2000-10-01"));
        another.setDuration(12);
        another.setMpa(mpaRepository.findById(1));

        Genre g1 = genreRepository.findById(1);
        another.setGenres(new LinkedHashSet<>(Set.of(g1)));

        filmRepository.create(another);

        Collection<Film> films = filmRepository.findAll();
        assertTrue(films.size() >= 2);
    }
}
