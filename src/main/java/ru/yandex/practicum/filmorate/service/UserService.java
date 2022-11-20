package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private int nextId = 1;
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // получить пользователя
    public User get(int id) {
        User user = userStorage.get(id);
        if (user == null) {
            throw new UserNotFoundException(String.format("Не найден пользователь с id %d", id));
        }
        return user;
    }

    // получить список всех пользователей
    public List<User> getAll() {
        return userStorage.getAll();
    }

    // добавить пользователя
    public User add(User user) {
        user.setId(nextId++);
        userStorage.add(user);
        return user;
    }

    // обновить данные пользователя
    public User update(User user) {
        checkIdOnUpdate(user);
        userStorage.update(user);
        return user;
    }

    // добавить в список друзей
    public User addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new ValidationException("Переданы одинаковые ID");
        }

        User user = get(userId);
        User friend = get(friendId);

        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());

        return user;
    }

    // удалить из списка друзей
    public User removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new ValidationException("Переданы одинаковые ID");
        }

        User user = get(userId);
        User friend = get(friendId);

        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());

        return user;
    }

    // получить список друзей пользователя
    public List<User> getFriends(int userId) {
        User user = get(userId);

        return user.getFriends().stream()
                .sorted()
                .map(userStorage::get)
                .collect(Collectors.toList());
    }

    // получить список общих друзей
    public List<User> getCommonFriends(int idUser1, int idUser2) {
        User user1 = get(idUser1);
        User user2 = get(idUser2);

        return user1.getFriends().stream()
                .filter(user2.getFriends()::contains)
                .sorted()
                .map(userStorage::get)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ---------------------------------------------

    private void checkIdOnUpdate(User user) throws UserNotFoundException {
        if (user.getId() == 0) {
            user.setId(nextId++);
            return;
        }
        if (userStorage.get(user.getId()) == null) {
            throw new UserNotFoundException(String.format("Не найден пользователь с id %d", user.getId()));
        }
    }
}
