package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private int nextId = 1;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    // получить фильм
    public Film get(int filmId) {
        Film film = filmStorage.get(filmId);
        if (film == null) {
            throw new FilmNotFoundException(String.format("Не найден фильм с id %d", filmId));
        }
        return film;
    }

    // получить все фильмы
    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    // добавить фильм
    public Film add(Film film) {
        film.setId(nextId++);
        filmStorage.add(film);
        return film;
    }

    // обновить данные о фильме
    public Film update(Film film) {
        checkIdOnUpdate(film);
        filmStorage.update(film);
        return film;
    }

    // поставить лайк
    public Film addLike(int filmId, int userId) {
        Film film = get(filmId);
        User user = getUser(userId);
        film.getLikes().add(user.getId());
        return film;
    }

    // удалить лайк
    public Film removeLike(int filmId, int userId) {
        Film film = get(filmId);
        User user = getUser(userId);
        film.getLikes().remove(user.getId());
        return film;
    }

    // получить список самых популярных фильмов
    public List<Film> getTopFilms(Integer size) {
        return filmStorage.getAll().stream()
                .sorted(this::compareLikes)
                .limit(size)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ---------------------------------------------

    // проверить ID при обновлении
    private void checkIdOnUpdate(Film film) {
        if (film.getId() == null) {
            film.setId(nextId++);
            return;
        }
        if (filmStorage.get(film.getId()) == null) {
            throw new FilmNotFoundException(String.format("Не найден фильм с id %d", film.getId()));
        }
    }

    // получить пользователя по ID
    private User getUser(int id) {
        User user = userStorage.get(id);
        if (user == null) {
            throw new UserNotFoundException(String.format("Не найден пользователь с id %d", id));
        }
        return user;
    }

    // сравнить фильмы по количеству лайков
    private int compareLikes(Film film1, Film film2) {
        return -1 * Integer.compare(film1.getLikes().size(), film2.getLikes().size());
    }
}
