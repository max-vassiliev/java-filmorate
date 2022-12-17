package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;

@Repository("filmsDb")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    // получить фильм
    @Override
    public Film get(int id) {
        final String sqlQuery = "select FILM_ID, NAME, DESCRIPTION, RELEASE_DATE, MPA, DURATION, LIKES " +
                "from FILMS where FILM_ID = ?";
        final List<Film> films = jdbcTemplate.query(sqlQuery, FilmDbStorage::makeFilm, id);
        if (films.isEmpty()) {
            return null;
        }

        Film film = films.get(0);
        loadFilmMpa(film);
        loadFilmGenres(film);
        loadFilmLikes(film);

        return film;
    }

    // получить все фильмы
    @Override
    public List<Film> getAll() {
        String sql = "select FILM_ID, NAME, DESCRIPTION, RELEASE_DATE, MPA, DURATION, LIKES from FILMS";
        final List<Film> films = jdbcTemplate.query(sql, FilmDbStorage::makeFilm);

        films.forEach(this::loadFilmMpa);
        films.forEach(this::loadFilmGenres);
        films.forEach(this::loadFilmLikes);
        return films;
    }

    // добавить фильм
    @Override
    public void add(Film film) {
        final String sqlQuery = "insert into FILMS (NAME, DESCRIPTION, RELEASE_DATE, MPA, DURATION, LIKES) " +
                "values (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"FILM_ID"});
            stmt.setString(1, film.getName());
            if (film.getDescription() == null) {
                stmt.setNull(2, Types.NULL);
            } else {
                stmt.setString(2, film.getDescription());
            }
            if (film.getReleaseDate() == null) {
                stmt.setNull(3, Types.NULL);
            } else {
                stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            }
            if (film.getMpa() == null) {
                stmt.setNull(4, Types.NULL);
            } else {
                stmt.setInt(4, film.getMpa().getId());
            }
            if (film.getDuration() == null) {
                stmt.setNull(5, Types.NULL);
            } else {
                stmt.setInt(5, film.getDuration());
            }
            stmt.setInt(6, film.getTotalLikes());
            return stmt;
        }, keyHolder);
        setFilmGenres(film);
    }

    // обновить данные о фильме
    @Override
    public void update(Film film) {
        final String sqlQuery = "update FILMS " +
                "set NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, MPA = ?, DURATION = ?, LIKES = ? " +
                "where FILM_ID = ?";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"FILM_ID"});
            stmt.setString(1, film.getName());
            if (film.getDescription() == null) {
                stmt.setNull(2, Types.NULL);
            } else {
                stmt.setString(2, film.getDescription());
            }
            if (film.getReleaseDate() == null) {
                stmt.setNull(3, Types.NULL);
            } else {
                stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            }
            if (film.getMpa() == null) {
                stmt.setNull(4, Types.NULL);
            } else {
                stmt.setInt(4, film.getMpa().getId());
            }
            if (film.getDuration() == null) {
                stmt.setNull(5, Types.NULL);
            } else {
                stmt.setInt(5, film.getDuration());
            }
            stmt.setInt(6, film.getTotalLikes());
            stmt.setInt(7, film.getId());
            return stmt;
        }, keyHolder);

        setFilmGenres(film);
    }

    // добавить лайк фильму
    public void addLike(int filmId, int userId) {
        final String sqlQuery = "merge into FILM_LIKE (FILM_ID, USER_ID) values (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    // удалить лайк у фильма
    public void removeLike(int filmId, int userId) {
        final String sqlQuery = "delete from FILM_LIKE where FILM_ID = ? and USER_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    // получить список самых популярных фильмов
    @Override
    public List<Film> getTopFilms(Integer size) {
        final String sql = "select FILM_ID, NAME, DESCRIPTION, RELEASE_DATE, MPA, DURATION, LIKES " +
                "from FILMS order by LIKES desc limit ?";
        final List<Film> films = jdbcTemplate.query(sql, FilmDbStorage::makeFilm, size);

        films.forEach(this::loadFilmMpa);
        films.forEach(this::loadFilmGenres);
        films.forEach(this::loadFilmLikes);
        return films;
    }


    // ---------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ---------------------------------------------

    // создать объект фильма
    static Film makeFilm(ResultSet rs, int id) throws SQLException {
        Film film = new Film(rs.getInt("FILM_ID"),
                rs.getString("NAME"),
                rs.getInt("LIKES")
        );
        if (rs.getInt("DURATION") == 0) {
            film.setDuration(null);
        } else {
            film.setDuration(rs.getInt("DURATION"));
        }
        if(rs.getString("DESCRIPTION") != null) {
            film.setDescription(rs.getString("DESCRIPTION"));
        }
        if (rs.getDate("RELEASE_DATE") != null) {
            film.setReleaseDate(rs.getDate("RELEASE_DATE").toLocalDate());
        }
        if (rs.getInt("MPA") > 0) {
            film.setMpa(new Mpa(rs.getInt("MPA"), null));
        }
        return film;
    }

    // добавить жанры фильма в базу
    private void setFilmGenres(Film film) {
        final String sqlQueryDelete = "delete from FILM_GENRE where FILM_ID = ?";
        final String sqlQueryInsert = "insert into FILM_GENRE (FILM_ID, GENRE_ID) values (?, ?)";

        jdbcTemplate.update(sqlQueryDelete, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sqlQueryInsert, film.getId(), genre.getId());
        }
    }

    // выгрузить жанры фильма из базы
    private void loadFilmGenres(Film film) {
        final String sqlQuery = "select g.GENRE_ID, g.GENRE " +
                "from GENRES g, FILM_GENRE fg " +
                "where fg.FILM_ID = ? " +
                "and fg.GENRE_ID = g.GENRE_ID";

        List<Genre> genres = jdbcTemplate.query(sqlQuery,
                (rs, rowNum) -> new Genre(rs.getInt("GENRE_ID"), rs.getString("GENRE")),
                film.getId());
        film.setGenres(new HashSet<>(genres));
    }

    // выгрузить из базы список лайков
    private void loadFilmLikes(Film film) {
        final String sqlQuery = "select * from FILM_LIKE where FILM_ID = ?";

        List<Integer> likes = jdbcTemplate.query(sqlQuery,
                (rs, rowNum) -> rs.getInt("USER_ID"), film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    // присвоить фильму рейтинг
    private void loadFilmMpa(Film film) {
        if (film.getMpa() == null) {
            return;
        }

        final String sqlQuery = "select MPA_NAME from MPA where MPA_ID = ?";
        List<String> mpaNames = jdbcTemplate.query(sqlQuery,
                (rs, rowNum) -> rs.getString("MPA_NAME"), film.getMpa().getId());
        film.getMpa().setName(mpaNames.get(0));
    }
}
