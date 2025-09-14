package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.Collection;

@Repository
@Slf4j
public class MpaRepository extends BaseRepository<Mpa> {
    private final MpaRowMapper mpaRowMapper;

    public MpaRepository(JdbcTemplate jdbcTemplate, MpaRowMapper mpaRowMapper) {
        super(jdbcTemplate);
        this.mpaRowMapper = mpaRowMapper;
    }

    public Collection<Mpa> findAll() {
        String sql = "SELECT * FROM mpa_rating;";
        return findMany(sql, mpaRowMapper);
    }

    public Mpa findById(long id) {
        String sql = "SELECT * FROM mpa_rating WHERE id = ?;";
        return findOne(sql, mpaRowMapper, id);
    }
}
