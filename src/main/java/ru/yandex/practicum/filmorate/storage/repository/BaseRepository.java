package ru.yandex.practicum.filmorate.storage.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbcTemplate;

    protected T findOne(String query, RowMapper<T> rowMapper, Object... params) {
        try {
            return jdbcTemplate.queryForObject(query, rowMapper, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    protected Collection<T> findMany(String query, RowMapper<T> rowMapper, Object... params) {
        try {
            return jdbcTemplate.query(query, rowMapper, params);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    protected boolean delete(String query, Object... params) {
        int rowsDeleted = jdbcTemplate.update(query, params);
        return rowsDeleted > 0;
    }

    protected void update(String query, Object... params) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        });
    }

    protected Long insert(String query, Object... params) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, generatedKeyHolder);

        Long id = generatedKeyHolder.getKeyAs(Long.class);

        if (id != null) {
            return id;
        } else {
            throw new NotFoundException("Data wasn't saved");
        }
    }

    protected Long insertMultipleKeys(String query, Object... params) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, generatedKeyHolder);

        Map<String, Object> keys = generatedKeyHolder.getKeys();

        if (keys != null && !keys.isEmpty()) {
            for (Object value : keys.values()) {
                if (value instanceof Long) {
                    return (Long) value;
                }
            }
        }
        throw new NotFoundException("Data wasn't saved or no valid key found");
    }
}
