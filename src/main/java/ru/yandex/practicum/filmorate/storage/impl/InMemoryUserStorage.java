package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("usersInMemory")
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User get(int id) {
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void add(User user) throws ValidationException {
        users.put(user.getId(), user);
    }

    @Override
    public void update(User user) throws ValidationException {
        users.put(user.getId(), user);
    }

    @Override
    public List<User> getFriends(int userId) {
        return null;
    }

    @Override
    public List<User> getCommonFriends(int idUser, int idOtherUser) {
        return null;
    }

    // TODO удалить
    @Override
    public void addFriend(int userId, int friendId) {
    }

    @Override
    public void removeFriend(int userId, int friendId) {
    }
}
