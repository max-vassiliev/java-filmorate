package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User get(int id);

    List<User> getAll();

    void add(User user);

    void update(User user);

}
