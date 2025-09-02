package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<Mpa> {

    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        var mpa = new Mpa();
        mpa.setId(rs.getLong("id"));
        mpa.setName(rs.getString("name"));
        mpa.setDescription(rs.getString("description"));
        return mpa;
    }
}
