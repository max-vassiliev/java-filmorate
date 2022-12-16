package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidationService;

import javax.validation.Valid;
import javax.validation.ValidationException;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {

    private final FilmService filmService;
    private final ValidationService validationService;

    @GetMapping("/{id}")
    public Film get(@PathVariable int id) {
        log.info("Get Film {}", id);
        return filmService.get(id);
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("Get all films");
        return filmService.getAll();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        log.info("Add film: {}", film);
        validationService.validate(film);
        return filmService.add(film);
    }

    @PutMapping
    public Film updateFilmWithoutId(@Valid @RequestBody Film film) {
        return update(film);
    }

    @PutMapping("/{id}")
    public Film update(@Valid @RequestBody Film film) {
        log.info("Update film: {}", film);
        validationService.validate(film);
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable int id,
                        @PathVariable int userId) {
        log.info("User{} liked Film{}", userId, id);
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable int id,
                           @PathVariable int userId) {
        log.info("User{} unliked Film{}", userId, id);
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getTopFilms(
            @RequestParam(defaultValue = "10", required = false) Integer count)
            throws ValidationException {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }

        log.info("Get top {} films", count);
        return filmService.getTopFilms(count);
    }

}
