package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository("genreDb")
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // получить все жанры
    @Override
    public List<Genre> getAll() {
        String sql = "select * from GENRES";
        final List<Genre> genres = jdbcTemplate.query(sql, GenreDbStorage::makeGenre);
        if (genres.isEmpty()) {
            return null;
        }
        return genres;
    }

    // получить жанр
    @Override
    public Genre get(int id) {
        final String sql = "select * from GENRES where GENRE_ID = ?";
        final List<Genre> genres = jdbcTemplate.query(sql, GenreDbStorage::makeGenre, id);
        if (genres.isEmpty()) {
            return null;
        }
        return genres.get(0);
    }

    // создать объект жанра Genre
    static Genre makeGenre(ResultSet rs, int id) throws SQLException {
        return new Genre(rs.getInt("GENRE_ID"),
                rs.getString("GENRE")
        );
    }
}
