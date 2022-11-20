package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ValidationException;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FilmControllerTest {

    private static Validator validator;
    private FilmController filmController;

    @BeforeAll
    public static void startValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void createFilmController() {
        filmController = new FilmController(
                new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage()),
                new ValidationService()
        );
    }

    // шаблон фильма со всеми полями
    private Film createFilm() {
        Film film = new Film();
        film.setName("Movie");
        film.setDescription("Movie description");
        film.setReleaseDate(LocalDate.of(2000, Month.DECEMBER, 28));
        film.setDuration(100);
        return film;
    }

    // ---------------------------------------------
    // POST /films
    // ---------------------------------------------

    // PASS: добавить фильм со всеми полями
    @Test
    void addFilmWithAllFields() {
        final Film film = createFilm();

        final Film savedFilm = filmController.add(film);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(0, violations.size());
        assertEquals(1, savedFilm.getId(), "Неверный ID");
        assertEquals(film.getName(), savedFilm.getName(), "Название не совпадает");
        assertEquals(film.getDescription(), savedFilm.getDescription(), "Описание не совпадает");
        assertEquals(film.getReleaseDate(), savedFilm.getReleaseDate(), "Дата не совпадает");
        assertEquals(film.getDuration(), savedFilm.getDuration(), "Продолжительность не совпадает");
    }

    // PASS: добавить фильм только с названием (минимально допустимый набор полей)
    @Test
    void addFilmWithMinimumFields() {
        final Film film = new Film();
        film.setName("Movie");

        final Film savedFilm = filmController.add(film);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(1, savedFilm.getId(), "Неверный ID");
        assertEquals(film.getName(), savedFilm.getName(), "Название фильма не совпадает");
    }

    // FAIL: добавить фильм с пустыми полями
    @Test
    void shouldFailToAddFilmIfFieldsAreEmpty() {
        final Film film = new Film();
        String errorMessageExpected = "Укажите название фильма";
        String errorMessageActual = null;

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (violations.iterator().hasNext())
            errorMessageActual = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Неверное количество ошибок");
        assertEquals(errorMessageExpected, errorMessageActual, "Текст ошибки не совпадает");
    }

    // FAIL: добавить фильм без названия
    @Test
    void shouldFailToAddFilmIfNameIsNull() {
        final Film film = new Film();
        String errorMessageExpected = "Укажите название фильма";
        String errorMessageActual = null;

        film.setDescription("Movie description");
        film.setReleaseDate(LocalDate.of(2000, Month.DECEMBER, 28));
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (violations.iterator().hasNext())
            errorMessageActual = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Неверное количество ошибок");
        assertEquals(errorMessageExpected, errorMessageActual, "Текст ошибки не совпадает");
    }


    // FAIL: описание фильма превышает 200 символов
    @Test
    void shouldFailToAddFilmIfDescriptionExceeds200Words() {
        String errorMessageExpected = "Описание не может превышать 200 символов";
        String errorMessageActual = null;

        final Film film = createFilm();
        film.setDescription(RandomStringUtils.randomAlphabetic(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (violations.iterator().hasNext())
            errorMessageActual = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Неверное количество ошибок");
        assertEquals(errorMessageExpected, errorMessageActual, "Текст ошибки не совпадает");
    }

    // PASS: максимальная длина названия (200 символов)
    @Test
    void shouldAddFilmWithMaxDescriptionSize() {
        final Film film = createFilm();
        film.setDescription(RandomStringUtils.randomAlphabetic(200));

        final Film savedFilm = filmController.add(film);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(0, violations.size());
        assertEquals(1, film.getId(), "Неверный ID");
        assertEquals(film.getName(), savedFilm.getName(), "Название не совпадает");
        assertEquals(film.getDescription(), savedFilm.getDescription(), "Описание не совпадает");
        assertEquals(film.getReleaseDate(), savedFilm.getReleaseDate(), "Дата не совпадает");
        assertEquals(film.getDuration(), savedFilm.getDuration(), "Продолжительность не совпадает");
    }

    // FAIL: дата выхода — 27 декабря 1895 (на день раньше минимально допустимой)
    @Test
    void shouldFailToAddFilmIfReleasedBeforeBirthOfCinema() {
        String expectedExceptionMessage = "Дата релиза не может быть раньше 28 декабря 1895 года";

        final Film film = createFilm();
        film.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 27));

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.add(film)
        );
        assertEquals(expectedExceptionMessage, exception.getMessage(), "Ожидалось другое сообщение об ошибке");
    }

    // PASS: дата выхода — 28 декабря 1895 (минимально допустимая дата)
    @Test
    void shouldFailToAddFilmIfReleasedOnBirthOfCinema() {
        final Film film = createFilm();
        film.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 28));

        Film savedFilm = filmController.add(film);
        List<Film> films = filmController.getAll();

        assertEquals(1, films.size(), "Неверное количество фильмов в списке");
        assertEquals(film.getName(), savedFilm.getName(), "Названия фильмов не совпадают");
        assertEquals(film.getReleaseDate(), savedFilm.getReleaseDate(), "Дата выхода не совпадает");
    }

    // FAIL: отрицательная продолжительность
    @Test
    public void shouldFailToAddFilmWithNegativeDuration() {
        String expectedErrorMessage = "Продолжительность фильма должна быть положительным числом";
        String actualErrorMessage = null;

        final Film film = createFilm();
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (violations.iterator().hasNext())
            actualErrorMessage = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Неверное количество ошибок");
        assertEquals(expectedErrorMessage, actualErrorMessage, "Текст ошибки не совпадает");
    }

    // FAIL: продолжительность фильма не должна быть равна нулю
    @Test
    public void shouldFailToAddFilmWhenDurationEqualsZero() {
        String expectedErrorMessage = "Продолжительность фильма должна быть положительным числом";
        String actualErrorMessage = null;

        final Film film = createFilm();
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (violations.iterator().hasNext())
            actualErrorMessage = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Неверное количество ошибок");
        assertEquals(expectedErrorMessage, actualErrorMessage, "Текст ошибки не совпадает");
    }

    // PASS: добавить фильм с минимально допустимой продолжительностью (1 минута)
    @Test
    void shouldAddFilmWhenDurationEquals1() {
        final Film film = createFilm();
        film.setDuration(1);

        final Film savedFilm = filmController.add(film);
        final List<Film> savedFilms = filmController.getAll();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(1, savedFilms.size(), "Сохранилось неверное количество фильмов");
        assertEquals(1, savedFilm.getId(), "Неверно определен ID");
        assertEquals(film.getName(), savedFilm.getName(), "Названия не совпадают");
        assertEquals(film.getDuration(), savedFilm.getDuration(), "Продолжительность не совпадает");
    }


    // ---------------------------------------------
    // PUT /films
    // ---------------------------------------------

    // PASS: обновить все поля фильма (нормальное поведение)
    @Test
    void updateFilm() {
        final Film filmToAdd = createFilm();

        final Film addedFilm = filmController.add(filmToAdd);

        final Film filmToUpdate = new Film();
        filmToUpdate.setId(addedFilm.getId());
        filmToUpdate.setName("Motion Picture");
        filmToUpdate.setDescription("Motion Picture description");
        filmToUpdate.setReleaseDate(LocalDate.of(2010, Month.DECEMBER, 25));
        filmToUpdate.setDuration(120);

        final Film updatedFilm = filmController.update(filmToUpdate);
        final List<Film> savedFilms = filmController.getAll();
        Set<ConstraintViolation<Film>> violations = validator.validate(updatedFilm);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(1, savedFilms.size(), "Неверное количество фильмов в списке");
        assertEquals(addedFilm.getId(), updatedFilm.getId(), "ID не совпадают");
        assertNotEquals(addedFilm.getName(), updatedFilm.getName(), "Названия не должны совпадать");
        assertNotEquals(addedFilm.getDescription(), updatedFilm.getDescription(), "Описание не должно совпадать");
        assertNotEquals(addedFilm.getReleaseDate(), updatedFilm.getReleaseDate(), "Даты не должны совпадать");
        assertNotEquals(addedFilm.getDuration(), updatedFilm.getDuration(), "Продолжительность не должна совпадать");
    }

    // FAIL: пустой запрос при обновлении
    @Test
    void shouldFailToUpdateWhenEmptyRequest() {
        String expectedErrorMessage = "Укажите название фильма";
        String actualErrorMessage = null;

        final Film filmToAdd = createFilm();
        final Film filmToUpdate = new Film();

        filmController.add(filmToAdd);

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.update(filmToUpdate)
        );
        assertEquals(expectedErrorMessage, exception.getMessage(), "Ожидалось другое сообщение об ошибке");

        Set<ConstraintViolation<Film>> violations = validator.validate(filmToUpdate);
        if (violations.iterator().hasNext())
            actualErrorMessage = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Обнаружено неверное количество ошибок");
        assertEquals(expectedErrorMessage, actualErrorMessage, "Ожидалось другое сообщение об ошибке");
    }


    // FAIL: при обновлении передается верный ID, но не заполнены остальные поля
    @Test
    void shouldFailToUpdateWhenCorrectIdButEmptyRequest() {
        String expectedExceptionMessage = "Укажите название фильма";
        String expectedErrorMessage = "Укажите название фильма";
        String actualErrorMessage = null;

        final Film filmToAdd = createFilm();
        final Film addedFilm = filmController.add(filmToAdd);

        final Film filmToUpdate = new Film();
        filmToUpdate.setId(addedFilm.getId());

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.update(filmToUpdate)
        );
        assertEquals(expectedExceptionMessage, exception.getMessage(), "Ожидалось другое сообщение об ошибке");

        Set<ConstraintViolation<Film>> violations = validator.validate(filmToUpdate);
        if (violations.iterator().hasNext())
            actualErrorMessage = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Количество ошибок не совпадает");
        assertEquals(expectedErrorMessage, actualErrorMessage, "Ожидалось другое сообщение об ошибке");
    }

    // PASS: при обновлении передается минимально необходимый набор полей
    @Test
    void shouldUpdateWhenOnlyRequiredFieldsArePassed() {
        final Film filmToAdd = createFilm();
        final Film addedFilm = filmController.add(filmToAdd);

        final Film filmToUpdate = new Film();
        filmToUpdate.setId(addedFilm.getId());
        filmToUpdate.setName("Motion Picture");

        final Film updatedFilm = filmController.update(filmToUpdate);
        final List<Film> savedFilms = filmController.getAll();
        final Film savedFilm = savedFilms.get(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(filmToUpdate);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(1, savedFilms.size(), "Неверное количество фильмов в списке");
        assertEquals(updatedFilm, savedFilm, "В таблице сохранен не тот фильм");
        assertNotEquals(addedFilm, savedFilm, "Объекты не должны совпадать");
    }

    // FAIL: при обновлении передается несуществующий ID
    @Test
    void shouldFailToUpdateWhenUsingUnknownID() {
        int testId = 1000;
        String expectedExceptionMessage = "Не найден фильм с id " + testId;
        final Film filmToAdd = createFilm();

        filmController.add(filmToAdd);

        final Film filmToUpdate = new Film();
        filmToUpdate.setId(testId);
        filmToUpdate.setName("Motion Picture");
        filmToUpdate.setDescription("Motion Picture description");
        filmToUpdate.setReleaseDate(LocalDate.of(2010, Month.DECEMBER, 25));
        filmToUpdate.setDuration(120);

        final FilmNotFoundException exception = assertThrows(
                FilmNotFoundException.class,
                () -> filmController.update(filmToUpdate)
        );
        assertEquals(expectedExceptionMessage, exception.getMessage(), "Ожидалось другое сообщение об ошибке");
    }

    // PASS: создать новый фильм, если в PUT-запросе не указан ID
    @Test
    void shouldAddFilmAsNewIfIdIsNullButFilmIsValid() {
        final Film filmToAdd = createFilm();

        final Film addedFilm = filmController.add(filmToAdd);

        final Film filmToUpdate = new Film();
        filmToUpdate.setName("Motion Picture");
        filmToUpdate.setDescription("Motion Picture description");
        filmToUpdate.setReleaseDate(LocalDate.of(2010, Month.DECEMBER, 25));
        filmToUpdate.setDuration(120);

        final Film updatedFilm = filmController.update(filmToUpdate);
        final List<Film> savedFilms = filmController.getAll();
        final Film savedFilm1 = savedFilms.get(0);
        final Film savedFilm2 = savedFilms.get(1);

        Set<ConstraintViolation<Film>> violations = validator.validate(updatedFilm);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(2, savedFilms.size(), "Неверное количество фильмов в списке");
        assertEquals(addedFilm, savedFilm1, "Фильмы не совпадают");
        assertEquals(updatedFilm, savedFilm2, "Фильмы не совпадают");
    }

}
