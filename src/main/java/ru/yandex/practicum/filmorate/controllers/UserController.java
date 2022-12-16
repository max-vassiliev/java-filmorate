package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.ValidationService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final ValidationService validationService;

    @GetMapping("/{id}")
    public User get(@PathVariable int id) {
        log.info("Get User{}", id);
        return userService.get(id);
    }

    @GetMapping
    public List<User> getAll() {
        log.info("Get all users");
        return userService.getAll();
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        log.info("Add user: {}", user);
        validationService.validate(user);
        return userService.add(user);
    }

    @PutMapping
    public User updateWithOutId(@Valid @RequestBody User user) {
        return update(user);
    }

    @PutMapping("/{id}")
    public User update(@Valid @RequestBody User user) {
        log.info("Update user: {}", user);
        validationService.validate(user);
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable int id,
                          @PathVariable int friendId) {
        log.info("Befriend User{} and User{}", id, friendId);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable int id,
                             @PathVariable int friendId) {
        log.info("Unfriend User{} and User{}", id, friendId);
        return userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        log.info("Get friends of User{}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id,
                                       @PathVariable int otherId) {
        log.info("Get common friends of User{} and User{}", id, otherId);
        return userService.getCommonFriends(id, otherId);

    }
}
