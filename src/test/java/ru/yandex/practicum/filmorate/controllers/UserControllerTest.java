package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.ValidationService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ValidationException;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UserControllerTest {

    private static Validator validator;
    private UserController userController;

    @BeforeAll
    public static void startValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void createUserController() {
        userController = new UserController(
                new UserService(new InMemoryUserStorage()),
                new ValidationService()
        );
    }

    // шаблон: получить пользователя со всеми данными
    private User createUser() {
        User user = new User();
        user.setEmail("pxl@example.com");
        user.setLogin("pxl2000");
        user.setName("Pixel");
        user.setBirthday(LocalDate.of(2000, Month.JANUARY, 1));
        return user;
    }

    // ---------------------------------------------
    // POST /users
    // ---------------------------------------------

    // PASS: добавить пользователя со всеми данными
    @Test
    void addUserWithAllFields() {
        final User user = createUser();

        final User addedUser = userController.add(user);
        final List<User> savedUsers = userController.getAll();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(1, savedUsers.size(), "Сохранено неверное число пользователей");
        assertEquals(1, addedUser.getId(), "Неверный ID");
        assertEquals(user.getEmail(), addedUser.getEmail(), "Адреса почты не совпадают");
        assertEquals(user.getLogin(), addedUser.getLogin(), "Логины не совпадают");
        assertEquals(user.getName(), addedUser.getName(), "Имена не совпадают");
        assertEquals(user.getBirthday(), addedUser.getBirthday(), "Дни рождения не совпадают");
    }


    // FAIL: добавить пользователя без данных
    @Test
    void shouldFailToAddUserIfAllFieldsAreEmpty() {
        String emailExpectedError = "Необходимо указать адрес электронной почты";
        String loginExpectedError = "Необходимо указать логин";
        String emailActualError = null;
        String loginActualError = null;

        final User user = new User();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        for (ConstraintViolation<User> violation : violations) {
            if (violation.getMessage().contains("адрес электронной почты"))
                emailActualError = violation.getMessage();
            if (violation.getMessage().contains("логин"))
                loginActualError = violation.getMessage();
        }

        assertEquals(2, violations.size(), "Неверное количество ошибок");
        assertEquals(loginExpectedError, loginActualError, "Текст ошибки не совпадает");
        assertEquals(emailExpectedError, emailActualError, "Текст ошибки не совпадает");
    }

    // PASS: добавить пользователя с обязательными данными (имейл и логин)
    @Test
    void addUserWithMinimalData() {
        final User user = new User();
        user.setEmail("pxl@example.com");
        user.setLogin("pxl2000");

        final User addedUser = userController.add(user);
        final List<User> savedUsers = userController.getAll();
        final Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(1, savedUsers.size(), "Сохранено неверное число пользователей");

        assertEquals(1, addedUser.getId(), "Неверно добавлен ID");
        assertEquals(user.getEmail(), addedUser.getEmail(), "Адреса почты не совпадают");
        assertEquals(user.getLogin(), addedUser.getLogin(), "Логины не совпадают");
    }

    // PASS: записать логин в поле name, если пользователь не указал имя
    @Test
    void shouldSaveLoginAsNameIfNameIsNull() {
        final User user = new User();
        user.setEmail("pxl@example.com");
        user.setLogin("pxl2000");

        final User addedUser = userController.add(user);
        final Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertEquals(user.getLogin(), addedUser.getLogin(), "Логины не совпадают");
        assertEquals(addedUser.getLogin(), addedUser.getName(), "Имя и логин должны совпадать");
        assertEquals(0, violations.size(), "Ошибок быть не должно");
    }

    // FAIL: не указан логин
    @Test
    void shouldFailToAddUserWithNoLogin() {
        String expectedErrorMessage = "Необходимо указать логин";
        String actualErrorMessage = null;

        final User user = createUser();
        user.setLogin(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.iterator().hasNext())
            actualErrorMessage = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Ожидалось другое количество ошибок");
        assertEquals(expectedErrorMessage, actualErrorMessage, "Ожидалось другое сообщение об ошибке");
    }

    // FAIL: не указан логин
    @Test
    void shouldFailToAddUserWithWhitespaceInLogin() {
        String expectedExceptionMessage = "В логине нельзя использовать пробелы";
        final User user = createUser();
        user.setLogin("pxl 2000");

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.add(user)
        );
        assertEquals(expectedExceptionMessage, exception.getMessage(), "Ожидалось другое сообщение об ошибке");
    }

    // FAIL: не указана почта
    @Test
    void shouldFailToAddUserWithNoEmail() {
        String expectedErrorMessage = "Необходимо указать адрес электронной почты";
        String actualErrorMessage = null;

        final User user = createUser();
        user.setEmail(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.iterator().hasNext())
            actualErrorMessage = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Ожидалось другое количество ошибок");
        assertEquals(expectedErrorMessage, actualErrorMessage, "Ожидалось другое сообщение об ошибке");
    }

    // FAIL: адрес электронной почты не соответствует формату
    @Test
    void shouldFailValidationIfEmailIsNotWellFormed() {
        String expectedErrorMessage = "Проверьте написание адреса электронной почты";
        String actualErrorMessage = null;

        final User user = createUser();
        user.setEmail("invalidEmail@");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.iterator().hasNext())
            actualErrorMessage = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Ожидалось другое количество ошибок");
        assertEquals(expectedErrorMessage, actualErrorMessage, "Ожидалось другое сообщение об ошибке");
    }

    // FAIL: дата рождения в будущем (завтра)
    @Test
    void shouldFailValidationIfBirthdayIsInTheFuture() {
        String expectedErrorMessage = "Дата рождения не может быть в будущем";
        String actualErrorMessage = null;

        final User user = createUser();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        user.setBirthday(tomorrow);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.iterator().hasNext())
            actualErrorMessage = violations.iterator().next().getMessage();

        assertEquals(1, violations.size(), "Ожидалось другое количество ошибок");
        assertEquals(expectedErrorMessage, actualErrorMessage, "Ожидалось другое сообщение об ошибке");
    }

    // PASS: день рождения сегодня
    @Test
    void shouldPassValidationIfBirthdayIsToday() {
        final User user = createUser();
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
    }


    // ---------------------------------------------
    // PUT /users
    // ---------------------------------------------

    // PASS: обновить данные пользователя (нормальное поведение)
    @Test
    void updateUserData() {
        final User userToAdd = createUser();
        final User addedUser = userController.add(userToAdd);

        final User userToUpdate = new User();
        userToUpdate.setId(addedUser.getId());
        userToUpdate.setEmail("pxl2000@example.com");
        userToUpdate.setLogin("2000pxl");
        userToUpdate.setName("PiXl");
        userToUpdate.setBirthday(LocalDate.now());

        final User updatedUser = userController.update(userToUpdate);
        final List<User> savedUsers = userController.getAll();
        Set<ConstraintViolation<User>> violations = validator.validate(updatedUser);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(1, savedUsers.size(), "Неверное число пользователей в списке");
        assertEquals(addedUser.getId(), updatedUser.getId(), "ID не совпадают");
        assertNotEquals(addedUser.getEmail(), updatedUser.getEmail(), "Email не должен совпадать");
        assertNotEquals(addedUser.getLogin(), updatedUser.getLogin(), "Логины не должны совпадать");
        assertNotEquals(addedUser.getName(), updatedUser.getName(), "Имена не должны совпадать");
        assertNotEquals(addedUser.getBirthday(), updatedUser.getBirthday(), "Дни рождения не должны совпадать");
    }

    // FAIL: пустой запрос при обновлении данных пользователя
    @Test
    void shouldFailToUpdateUserWhenEmptyRequest() {
        String loginExpectedError = "Необходимо указать логин";
        String emailExpectedError = "Необходимо указать адрес электронной почты";
        String loginActualError = null;
        String emailActualError = null;

        final User userToAdd = createUser();
        final User userToUpdate = new User();

        userController.add(userToAdd);

        Set<ConstraintViolation<User>> violations = validator.validate(userToUpdate);
        for (ConstraintViolation<User> violation : violations) {
            if (violation.getMessage().contains("адрес электронной почты"))
                emailActualError = violation.getMessage();
            if (violation.getMessage().contains("логин"))
                loginActualError = violation.getMessage();
        }

        assertEquals(2, violations.size(), "Неверное количество ошибок");
        assertEquals(loginExpectedError, loginActualError, "Текст ошибки не совпадает");
        assertEquals(emailExpectedError, emailActualError, "Текст ошибки не совпадает");
    }

    // FAIL: при обновлении передается верный ID, но не заполнены остальные необходимые поля
    @Test
    void shouldFailToUpdateWhenCorrectIdButEmptyRequest() {
        String loginExpectedError = "Необходимо указать логин";
        String emailExpectedError = "Необходимо указать адрес электронной почты";
        String loginActualError = null;
        String emailActualError = null;

        final User userToAdd = createUser();
        final User addedUser = userController.add(userToAdd);

        final User userToUpdate = new User();
        userToUpdate.setId(addedUser.getId());

        Set<ConstraintViolation<User>> violations = validator.validate(userToUpdate);
        for (ConstraintViolation<User> violation : violations) {
            if (violation.getMessage().contains("адрес электронной почты"))
                emailActualError = violation.getMessage();
            if (violation.getMessage().contains("логин"))
                loginActualError = violation.getMessage();
        }

        assertEquals(2, violations.size(), "Неверное количество ошибок");
        assertEquals(loginExpectedError, loginActualError, "Текст ошибки не совпадает");
        assertEquals(emailExpectedError, emailActualError, "Текст ошибки не совпадает");
    }

    // FAIL: при обновлении передается несуществующий ID
    @Test
    void shouldFailToUpdateWhenUsingUnknownID() {
        int testId = 1000;
        String expectedExceptionMessage = "Не найден пользователь с id " + testId;
        final User userToAdd = createUser();

        userController.add(userToAdd);

        final User userToUpdate = new User();
        userToUpdate.setId(testId);
        userToUpdate.setEmail("pxl2000@example.com");
        userToUpdate.setLogin("2000pxl");
        userToUpdate.setName("PiXl");
        userToUpdate.setBirthday(LocalDate.now());

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.update(userToUpdate)
        );
        assertEquals(expectedExceptionMessage, exception.getMessage(), "Ожидалось другое сообщение об ошибке");
    }

    // PASS: создать нового пользователя, если в PUT-запросе не указан ID
    @Test
    void shouldAddUserAsNewIfIdIsNullButDataIsValid() {
        final User userToAdd = createUser();

        final User addedUser = userController.add(userToAdd);

        final User userToUpdate = new User();
        userToUpdate.setEmail("pxl2000@example.com");
        userToUpdate.setLogin("2000pxl");
        userToUpdate.setName("PiXl");
        userToUpdate.setBirthday(LocalDate.now());

        final User updatedUser = userController.update(userToUpdate);
        final List<User> savedUsers = userController.getAll();
        final User savedUser1 = savedUsers.get(0);
        final User savedUser2 = savedUsers.get(1);

        Set<ConstraintViolation<User>> violations = validator.validate(updatedUser);

        assertEquals(0, violations.size(), "Ошибок быть не должно");
        assertEquals(2, savedUsers.size(), "В списке должно быть два пользователя");
        assertEquals(addedUser, savedUser1, "Объекты должны совпадать");
        assertEquals(updatedUser, savedUser2, "Объекты должны совпадать");
    }

}