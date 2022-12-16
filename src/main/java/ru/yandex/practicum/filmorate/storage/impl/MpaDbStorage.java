package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository("mpaDb")
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // получить все рейтинги
    @Override
    public List<Mpa> getAll() {
        String sql = "select * from MPA";
        final List<Mpa> mpaList = jdbcTemplate.query(sql, MpaDbStorage::makeMpa);
        if (mpaList.isEmpty()) {
            return null;
        }
        return mpaList;
    }

    // получить рейтинг
    @Override
    public Mpa get(int id) {
        final String sql = "select * from MPA where MPA_ID = ?";
        final List<Mpa> mpaList = jdbcTemplate.query(sql, MpaDbStorage::makeMpa, id);
        if (mpaList.isEmpty()) {
            return null;
        }
        return mpaList.get(0);
    }

    // создать объект рейтинга (Mpa)
    static Mpa makeMpa(ResultSet rs, int id) throws SQLException {
        return new Mpa(rs.getInt("MPA_ID"),
                rs.getString("MPA_NAME")
        );
    }
}
