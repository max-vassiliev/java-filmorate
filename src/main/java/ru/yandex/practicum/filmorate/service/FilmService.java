package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private int nextId = 1;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;


    // получить фильм
    public Film get(int filmId) {
        Film film = filmStorage.get(filmId);
        if (film == null) {
            throw new EntityNotFoundException(String.format("Не найден фильм с id %d", filmId), Film.class);
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
        film.setTotalLikes(film.getLikes().size());
        filmStorage.update(film);
        filmStorage.addLike(film.getId(), user.getId());
        return film;
    }

    // удалить лайк
    public Film removeLike(int filmId, int userId) {
        Film film = get(filmId);
        User user = getUser(userId);

        film.getLikes().remove(user.getId());
        film.setTotalLikes(film.getLikes().size());
        filmStorage.update(film);
        filmStorage.removeLike(film.getId(), user.getId());
        return film;
    }

    // получить список самых популярных фильмов
    public List<Film> getTopFilms(Integer size) {
        return filmStorage.getTopFilms(size);
    }

    // ---------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ---------------------------------------------

    // проверить ID при обновлении
    private void checkIdOnUpdate(Film film) {
        if (film.getId() == null) {
            add(film);
        }
        if (filmStorage.get(film.getId()) == null) {
            throw new EntityNotFoundException(String.format("Не найден фильм с id %d", film.getId()), Film.class);
        }
    }

    // получить пользователя по ID
    private User getUser(int id) {
        User user = userStorage.get(id);
        if (user == null) {
            throw new EntityNotFoundException(String.format("Не найден пользователь с id %d", id), User.class);
        }
        return user;
    }
}
