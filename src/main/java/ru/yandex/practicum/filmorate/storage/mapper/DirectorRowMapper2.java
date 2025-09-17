package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DirectorRowMapper2 implements RowMapper<Director> {
    @Override
    public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            if (rs.getLong("director_id") == 0) {
                return null;
            }

            Director director = new Director();
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("director_name"));
            return director;
        } catch (SQLException e) {
            return null;
        }
    }
}