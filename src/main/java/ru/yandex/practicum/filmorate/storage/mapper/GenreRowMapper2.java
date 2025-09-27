package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GenreRowMapper2 implements RowMapper<Genre> {
    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = (Long) rs.getObject("genre_id");
        if (id == null) return null;

        Genre g = new Genre();
        g.setId(id);
        g.setName(rs.getString("genre_name"));
        return g;
    }
}
