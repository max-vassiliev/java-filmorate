package ru.yandex.practicum.filmorate.controllers;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.ValidationException;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895,  Month.DECEMBER, 28);
    private int nextId = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) throws ValidationException {
        try {
            checkReleaseDate(film.getReleaseDate());
            film.setId(nextId++);
            films.put(film.getId(), film);
            log.info("Запрос к эндпойнту POST /films - " +
                    "Сохранен фильм: " + film);
            return film;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обращении к эндпойнту POST /films: " +
                    "{}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) throws ValidationException {
         try {
             checkReleaseDate(film.getReleaseDate());
             checkIdOnUpdate(film);
             films.put(film.getId(), film);
             log.info("Запрос к эндпойнту PUT /films" +
                     " Сохранен фильм: " + film);
             return film;
         } catch (ValidationException e) {
             log.warn("Ошибка при обращении к эндпойнту PUT /films: " +
                     "{}", e.getMessage(), e);
             throw e;
         }
    }

    // проверить, что фильм вышел не раньше дня создания кино
    private void checkReleaseDate(LocalDate releaseDate) throws ValidationException {
        if (releaseDate == null) return;
        if (BIRTH_OF_CINEMA.isAfter(releaseDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    // проверить ID при обновлении
    private void checkIdOnUpdate(Film film) throws ValidationException {
        if (film.getId() == null && film.getName() == null) {
            throw new ValidationException("При обновлении передан пустой запрос");
        }
        if (film.getName() == null) {
            throw new ValidationException("Укажите название фильма");
        }
        if (film.getId() == null) {
            film.setId(nextId++);
            return;
        }
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("В таблице нет фильма с таким ID");
        }
    }
}
