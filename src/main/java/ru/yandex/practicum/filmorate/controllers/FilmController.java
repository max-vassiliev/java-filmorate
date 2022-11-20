package ru.yandex.practicum.filmorate.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidationService;

import javax.validation.Valid;
import javax.validation.ValidationException;

import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;
    private final ValidationService validationService;

    @Autowired
    public FilmController(FilmService filmService, ValidationService validationService) {
        this.filmService = filmService;
        this.validationService = validationService;
    }

    @GetMapping("/{id}")
    public Film get(@PathVariable int id) {
        log.info("Get Film{}", id);
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
