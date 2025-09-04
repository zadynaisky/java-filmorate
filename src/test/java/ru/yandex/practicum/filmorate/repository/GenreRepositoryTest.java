package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.repository.GenreRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({GenreRepository.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreRepositoryTest {
    private final GenreRepository genreRepository;

    @Test
    void testFindById() {
        Genre genre = genreRepository.findById(1);
        assertEquals(1, genre.getId());
        assertNotNull(genre.getName());
    }

    @Test
    void testFindAll() {
        Collection<Genre> genres = genreRepository.findAll();
        assertFalse(genres.isEmpty());
    }

    @Test
    void testFindByFilmId() {
        Collection<Genre> genres = genreRepository.findByFilmId(1);
        assertNotNull(genres);
    }
}
