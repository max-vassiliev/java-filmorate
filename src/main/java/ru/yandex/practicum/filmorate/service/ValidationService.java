package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.Month;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private static final LocalDate CINEMA_CREATED = LocalDate.of(1895,  Month.DECEMBER, 28);

    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    // валидация пользователя
    public void validate(User user) throws ValidationException {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("В логине нельзя использовать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    // валидация фильма
    public void validate(Film film) {
        if (film.getMpa() != null && mpaStorage.get(film.getMpa().getId()) == null) {
            throw new ValidationException("Неверно указан ID рейтинга MPAA");
        }
        validateFilmGenre(film);
        if (film.getReleaseDate() == null) return;
        if (CINEMA_CREATED.isAfter(film.getReleaseDate())) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    // валидация жанра
    private void validateFilmGenre(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            if (genreStorage.get(genre.getId()) == null) {
                throw new ValidationException("Нет жанра с id=" + genre.getId());
            }
        }
    }
}
