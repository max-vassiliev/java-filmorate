package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.Month;

@Service
public class ValidationService {

    private static final LocalDate CINEMA_CREATED = LocalDate.of(1895,  Month.DECEMBER, 28);

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
        if (film.getReleaseDate() == null) return;
        if (CINEMA_CREATED.isAfter(film.getReleaseDate())) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
