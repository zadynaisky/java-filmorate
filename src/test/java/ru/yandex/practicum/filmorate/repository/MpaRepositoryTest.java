package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.repository.MpaRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaRepository.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaRepositoryTest {
    private final MpaRepository mpaRepository;

    @Test
    void testFindById() {
        Mpa rating = mpaRepository.findById(1);
        assertEquals(1, rating.getId());
        assertNotNull(rating.getName());
    }

    @Test
    void testFindAll() {
        Collection<Mpa> ratings = mpaRepository.findAll();
        assertFalse(ratings.isEmpty());
    }
}
