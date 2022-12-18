package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private int nextId = 1;
    private final UserStorage userStorage;


    // получить пользователя
    public User get(int id) {
        User user = userStorage.get(id);
        if (user == null) {
            throw new EntityNotFoundException(String.format("Не найден пользователь с id %d", id), User.class);
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
        userStorage.addFriend(user.getId(), friend.getId());

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
        userStorage.removeFriend(user.getId(), friend.getId());

        return user;
    }

    // получить друзей пользователя
    public List<User> getFriends(int userId) {
        User user = get(userId);
        return userStorage.getFriends(user.getId());
    }

    // получить список общих друзей
    public List<User> getCommonFriends(int idUser, int idOtherUser) {
        User user = get(idUser);
        User otherUser = get(idOtherUser);
        return userStorage.getCommonFriends(user.getId(), otherUser.getId());
    }

    // ---------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ---------------------------------------------

    private void checkIdOnUpdate(User user) throws EntityNotFoundException {
        if (user.getId() == 0) {
            add(user);
        }
        if (userStorage.get(user.getId()) == null) {
            throw new EntityNotFoundException(
                    String.format("Не найден пользователь с id %d", user.getId()),
                    User.class
            );
        }
    }
}
