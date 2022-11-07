package ru.yandex.practicum.filmorate.controllers;

import ru.yandex.practicum.filmorate.model.User;

import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private int nextId = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        try {
            checkRequiredData(user);
            checkName(user);
            user.setId(nextId++);
            users.put(user.getId(), user);
            log.info("Запрос к эндпойнту POST /users - " +
                    "Добавлен пользователь: " + user);
            return user;
        } catch (ValidationException e) {
            log.warn("Ошибка при обращении к эндпойнту POST /users: " +
                    "{}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) throws ValidationException {
        try {
            checkBeforeUpdate(user);
            checkRequiredData(user);
            checkName(user);
            checkIdOnUpdate(user);
            users.put(user.getId(), user);
            log.info("Запрос к эндпойнту PUT /users" +
                    "Добавлен данные о пользователе: " + user);
            return user;
        } catch (ValidationException e) {
            log.warn("Ошибка при обращении к эндпойнту PUT /users: " +
                    "{}", e.getMessage(), e);
            throw e;
        }
    }

    private void checkRequiredData(User user) throws ValidationException {
        if (user.getEmail() == null && user.getLogin() == null) {
            throw new ValidationException("Необходимо указать адрес электронной почты и логин");
        }
        if (user.getEmail() == null) {
            throw new ValidationException("Необходимо указать адрес электронной почты");
        }
        if (user.getLogin() == null) {
            throw new ValidationException("Необходимо указать логин");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("В логине нельзя использовать пробелы");
        }
    }

    // если имя не указано, записать логин в поле name
    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    // проверить необходимые данные для обновления
    private void checkBeforeUpdate(User user) throws ValidationException {
        if (user.getId() == 0 && user.getEmail() == null && user.getLogin() == null) {
            throw new ValidationException("Необходимо указать ID, адрес электронной почты и логин пользователя");
        }
    }

    private void checkIdOnUpdate(User user) throws ValidationException {
        if (user.getId() == 0) {
            user.setId(nextId++);
            return;
        }
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Нет пользователя с таким ID");
        }
    }
}
