package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    // добавить и получить пользователя
    @Test
    void addUser() {
        int expectedId = 1;
        User user = createUser();
        userStorage.add(user);
        User savedUser = userStorage.get(expectedId);

        assertEquals(user.getEmail(), savedUser.getEmail(), "Поля email не совпадают");
        assertEquals(user.getLogin(), savedUser.getLogin(), "Поля login не совпадают");
        assertEquals(user.getName(), savedUser.getName(), "Поля name не совпадают");
    }

    // получить пользователя — ID нет в базе
    @Test
    void getUserWithIdNotInTheDatabase() {
        int idNotInDatabase = 2;
        User user = createUser();
        userStorage.add(user);

        User savedUser = userStorage.get(idNotInDatabase);
        assertNull(savedUser, "Ожидалось null");
    }

    // получить пользователя — база пустая
    @Test
    void getUserFromEmptyDatabase() {
        int idNotInDatabase = 1;
        User user = userStorage.get(idNotInDatabase);
        assertNull(user, "Ожидалось null");
    }

    // получить всех пользователей
    @Test
    void getAllUsers() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        userStorage.add(user);
        userStorage.add(anotherUser);

        List<User> users = userStorage.getAll();

        assertEquals(2, users.size(), "Получили неверное число пользователей");
        assertEquals(user.getEmail(), users.get(0).getEmail(), "Неверно сохранены данные пользователя");
        assertEquals(anotherUser.getEmail(), users.get(1).getEmail(), "Неверно сохранены данные пользователя");
    }

    // получить всех пользователей — база пустая
    @Test
    void getAllUsersFromEmptyDatabase() {
        List<User> users = userStorage.getAll();
        assertEquals(0, users.size(), "Список должен быть пустым");
    }

    // обновить данные о пользователе
    @Test
    void updateUser() {
        int expectedId = 1;
        User user = createUser();

        userStorage.add(user);

        User updatedUser = createAnotherUser();
        updatedUser.setId(expectedId);

        userStorage.update(updatedUser);

        User savedUser = userStorage.get(expectedId);

        assertEquals(updatedUser.getEmail(), savedUser.getEmail(), "Поля email не совпадают");
        assertNotEquals(user.getEmail(), savedUser.getEmail(), "Поля email совпадают");
    }

    // обновить данные о пользователе — ID нет в базе
    @Test
    void updateUserWithIdNotInDatabase() {
        int idNotInDatabase = 2;
        User user = createUser();

        userStorage.add(user);

        User updatedUser = createAnotherUser();
        updatedUser.setId(idNotInDatabase);

        userStorage.update(updatedUser);

        List<User> users = userStorage.getAll();
        assertEquals(1, users.size(),
                "В базе неверное число пользователей");
        assertEquals(user.getEmail(), users.get(0).getEmail(),
                "В базе сохранены не те данные");
        assertNotEquals(updatedUser.getEmail(), users.get(0).getEmail(),
                "Обновленные данные не должны быть сохранены");
    }

    // обновить данные о пользователе — база пустая
    @Test
    void updateUserInEmptyDatabase() {
        int idNotInDatabase = 1;
        User user = createUser();
        user.setId(idNotInDatabase);

        userStorage.update(user);
        List<User> users = userStorage.getAll();
        User savedUser = userStorage.get(idNotInDatabase);

        assertEquals(0, users.size(), "Список пользователей должен быть пустым");
        assertNull(savedUser, "Ожидалось null");
    }

    // получить друзей пользователя
    @Test
    void getFriendsOfUser() {
        User user = createUser();
        User sonya = createSonya();
        User tony = createTony();
        int userExpectedId = 1;
        int sonyaExpectedId = 2;
        int tonyExpectedId = 3;

        userStorage.add(user);
        userStorage.add(sonya);
        userStorage.add(tony);

        userStorage.addFriend(userExpectedId, sonyaExpectedId);
        userStorage.addFriend(userExpectedId, tonyExpectedId);

        List<User> friends = userStorage.getFriends(userExpectedId);

        assertEquals(2, friends.size(), "Неверное число друзей");
        assertEquals(sonya.getEmail(), friends.get(0).getEmail(), "Поля email не совпадают");
        assertEquals(tony.getEmail(), friends.get(1).getEmail(), "Поля email не совпадают");
    }

    // получить друзей пользователя — ID пользователя нет в базе
    @Test
    void getFriendsWhenIdNotInDatabase() {
        int idNotInDatabase = 4;

        User user = createUser();
        User sonya = createSonya();
        User tony = createTony();
        int userExpectedId = 1;
        int sonyaExpectedId = 2;
        int tonyExpectedId = 3;

        userStorage.add(user);
        userStorage.add(sonya);
        userStorage.add(tony);

        userStorage.addFriend(userExpectedId, sonyaExpectedId);
        userStorage.addFriend(userExpectedId, tonyExpectedId);

        List<User> friends = userStorage.getFriends(idNotInDatabase);

        assertEquals(0, friends.size(), "Список должен быть пустым");
    }

    // получить друзей пользователя — список друзей пуст
    @Test
    void getFriendsWhenFriendsListIsEmpty() {
        User user = createUser();
        User sonya = createSonya();
        User tony = createTony();
        int userExpectedId = 1;

        userStorage.add(user);
        userStorage.add(sonya);
        userStorage.add(tony);

        List<User> userFriends = userStorage.getFriends(userExpectedId);
        List<User> allUsers = userStorage.getAll();

        assertEquals(0, userFriends.size(), "Список должен быть пустым");
        assertEquals(3, allUsers.size(), "Сохранен неверный список пользователей");
    }

    // получить друзей пользователя — база пустая
    @Test
    void getFriendsWhenDatabaseIsEmpty() {
        int someUserId = 1;

        List<User> userFriends = userStorage.getFriends(someUserId);
        List<User> allUsers = userStorage.getAll();

        assertEquals(0, userFriends.size(), "Список должен быть пустым");
        assertEquals(0, allUsers.size(), "В базе не должно быть пользователей");
    }

    // получить общих друзей
    @Test
    void getCommonFriends() {
        User user = createUser();
        User anotherUser = createAnotherUser();
        User sonya = createSonya();
        User tony = createTony();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;
        int sonyaExpectedId = 3;
        int tonyExpectedId = 4;

        userStorage.add(user);
        userStorage.add(anotherUser);
        userStorage.add(sonya);
        userStorage.add(tony);

        userStorage.addFriend(userExpectedId, sonyaExpectedId);
        userStorage.addFriend(userExpectedId, tonyExpectedId);
        userStorage.addFriend(anotherUserExpectedId, sonyaExpectedId);

        List<User> commonFriends = userStorage.getCommonFriends(userExpectedId, anotherUserExpectedId);

        assertEquals(1, commonFriends.size(), "Неверное число пользователей в списке");
        assertEquals(sonya.getEmail(), commonFriends.get(0).getEmail(), "Ожидался другой пользователь");
    }

    // получить общих друзей — ID пользователя нет в базе
    @Test
    void getCommonFriendsWhenUserIdNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();
        User sonya = createSonya();
        User tony = createTony();

        int idNotInDatabase = 5;
        int userExpectedId = 1;
        int anotherUserExpectedId = 2;
        int sonyaExpectedId = 3;
        int tonyExpectedId = 4;

        userStorage.add(user);
        userStorage.add(anotherUser);
        userStorage.add(sonya);
        userStorage.add(tony);

        userStorage.addFriend(userExpectedId, sonyaExpectedId);
        userStorage.addFriend(userExpectedId, tonyExpectedId);
        userStorage.addFriend(anotherUserExpectedId, sonyaExpectedId);

        List<User> commonFriends = userStorage.getCommonFriends(idNotInDatabase, anotherUserExpectedId);
        List<User> allUsers = userStorage.getAll();

        assertEquals(0, commonFriends.size(), "Список должен быть пустым");
        assertEquals(4, allUsers.size(), "Неверное число пользователей в базе");
    }

    // получить общих друзей — ID другого пользователя нет в базе
    @Test
    void getCommonFriendsWhenOtherUserIdNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();
        User sonya = createSonya();
        User tony = createTony();

        int idNotInDatabase = 5;
        int userExpectedId = 1;
        int anotherUserExpectedId = 2;
        int sonyaExpectedId = 3;
        int tonyExpectedId = 4;

        userStorage.add(user);
        userStorage.add(anotherUser);
        userStorage.add(sonya);
        userStorage.add(tony);

        userStorage.addFriend(userExpectedId, sonyaExpectedId);
        userStorage.addFriend(userExpectedId, tonyExpectedId);
        userStorage.addFriend(anotherUserExpectedId, sonyaExpectedId);

        List<User> commonFriends = userStorage.getCommonFriends(userExpectedId, idNotInDatabase);
        List<User> allUsers = userStorage.getAll();

        assertEquals(0, commonFriends.size(), "Список должен быть пустым");
        assertEquals(4, allUsers.size(), "Неверное число пользователей в базе");
    }

    // получить общих друзей — в базе нет двух передаваемых ID
    @Test
    void getCommonFriendsWhenBothIdsNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();
        User sonya = createSonya();
        User tony = createTony();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;
        int sonyaExpectedId = 3;
        int tonyExpectedId = 4;

        int idNotInDatabase = 5;
        int otherIdNotInDatabase = 6;

        userStorage.add(user);
        userStorage.add(anotherUser);
        userStorage.add(sonya);
        userStorage.add(tony);

        userStorage.addFriend(userExpectedId, sonyaExpectedId);
        userStorage.addFriend(userExpectedId, tonyExpectedId);
        userStorage.addFriend(anotherUserExpectedId, sonyaExpectedId);

        List<User> commonFriends = userStorage.getCommonFriends(idNotInDatabase, otherIdNotInDatabase);
        List<User> allUsers = userStorage.getAll();

        assertEquals(0, commonFriends.size(), "Список должен быть пустым");
        assertEquals(4, allUsers.size(), "Неверное число пользователей в базе");
    }

    // получить общих друзей — общих друзей нет
    @Test
    void getCommonFriendsWhenNoCommonFriends() {
        User user = createUser();
        User anotherUser = createAnotherUser();
        User sonya = createSonya();
        User tony = createTony();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;
        int sonyaExpectedId = 3;
        int tonyExpectedId = 4;

        userStorage.add(user);
        userStorage.add(anotherUser);
        userStorage.add(sonya);
        userStorage.add(tony);

        userStorage.addFriend(userExpectedId, tonyExpectedId);
        userStorage.addFriend(anotherUserExpectedId, sonyaExpectedId);

        List<User> commonFriends = userStorage.getCommonFriends(userExpectedId, anotherUserExpectedId);
        List<User> userFriends = userStorage.getFriends(userExpectedId);
        List<User> anotherUserFriends = userStorage.getFriends(anotherUserExpectedId);

        assertEquals(0, commonFriends.size(), "Список должен быть пустым");
        assertEquals(1, userFriends.size(), "Список не должен быть пустым");
        assertEquals(1, anotherUserFriends.size(), "Список не должен быть пустым");
    }

    // получить общих друзей — база пустая
    @Test
    void getCommonFriendsFromEmptyDatabase() {
        int idNotInDatabase = 1;
        int otherIdNotInDatabase = 2;

        List<User> commonFriends = userStorage.getCommonFriends(idNotInDatabase, otherIdNotInDatabase);
        List<User> users = userStorage.getAll();

        assertEquals(0, commonFriends.size(), "Список должен быть пустым");
        assertEquals(0, users.size(), "Список должен быть пустым");
    }

    // добавить в друзья — пользователь уже в друзьях
    @Test
    void addFriendWhenAlreadyFriends() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;

        userStorage.add(user);
        userStorage.add(anotherUser);

        userStorage.addFriend(userExpectedId, anotherUserExpectedId);

        List<User> friends = userStorage.getFriends(userExpectedId);

        userStorage.addFriend(userExpectedId, anotherUserExpectedId);

        List<User> friendsAfterAdding = userStorage.getFriends(userExpectedId);

        assertEquals(friends.size(), friendsAfterAdding.size(),
                "В списках должно быть одинаковое число пользователей");
        assertEquals(anotherUser.getEmail(), friends.get(0).getEmail(),
                "В списке не тот пользователь");
        assertEquals(anotherUser.getEmail(), friendsAfterAdding.get(0).getEmail(),
                "В списке не тот пользователь");
    }

    // добавить в друзья — ID пользователя нет в базе
    @Test
    void addFriendWhenUserIdNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int anotherUserExpectedId = 2;
        int idNotInDatabase = 3;

        userStorage.add(user);
        userStorage.add(anotherUser);

        assertThrows(Throwable.class,
                () -> userStorage.addFriend(idNotInDatabase, anotherUserExpectedId)
        );
    }

    // добавить в друзья — ID друга нет в базе
    @Test
    void addFriendWhenFriendIdNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int userExpectedId = 1;
        int idNotInDatabase = 3;

        userStorage.add(user);
        userStorage.add(anotherUser);

        assertThrows(Throwable.class,
                () -> userStorage.addFriend(userExpectedId, idNotInDatabase));
    }

    // добавить в друзья — в базе нет ID пользователя и его друга
    @Test
    void addFriendWhenBothIdsNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int idNotInDatabase = 3;
        int anotherIdNotInDatabase = 4;

        userStorage.add(user);
        userStorage.add(anotherUser);

        assertThrows(Throwable.class,
                () -> userStorage.addFriend(idNotInDatabase, anotherIdNotInDatabase));
    }

    // добавить в друзья — база пустая
    @Test
    void addFriendWhenDatabaseIsEmpty() {
        int idNotInDatabase = 1;
        int anotherIdNotInDatabase = 2;

        assertThrows(Throwable.class,
                () -> userStorage.addFriend(idNotInDatabase, anotherIdNotInDatabase));

        List<User> users = userStorage.getAll();
        assertEquals(0, users.size(), "База должна быть пустой");
    }

    // удалить из друзей
    @Test
    void removeFriend() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;

        userStorage.add(user);
        userStorage.add(anotherUser);

        userStorage.addFriend(userExpectedId, anotherUserExpectedId);

        List<User> friendsAfterAdd = userStorage.getFriends(userExpectedId);

        userStorage.removeFriend(userExpectedId, anotherUserExpectedId);

        List<User> friendsAfterRemove = userStorage.getFriends(userExpectedId);

        assertNotEquals(friendsAfterAdd.size(), friendsAfterRemove.size(),
                "В списках не должно быть одинаковое число пользователей");
        assertEquals(0, friendsAfterRemove.size(),
                "Список должен быть пустой");
    }

    // удалить из друзей — друга нет в списке друзей
    @Test
    void removeFriendWhenUsersAreNotFriends() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;

        userStorage.add(user);
        userStorage.add(anotherUser);

        List<User> userFriendsBeforeRemove = userStorage.getFriends(userExpectedId);

        userStorage.removeFriend(userExpectedId, anotherUserExpectedId);

        List<User> userFriendsAfterRemove = userStorage.getFriends(userExpectedId);

        assertEquals(userFriendsBeforeRemove.size(), userFriendsAfterRemove.size(),
                "Размеры списков не совпадают");
        assertEquals(0, userFriendsAfterRemove.size(),
                "Список должен быть пустой");
    }

    // удалить из друзей — ID пользователя нет в базе
    @Test
    void removeFriendWhenUserIdNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;
        int idNotInDatabase = 3;

        userStorage.add(user);
        userStorage.add(anotherUser);

        userStorage.addFriend(userExpectedId, anotherUserExpectedId);

        userStorage.removeFriend(idNotInDatabase, anotherUserExpectedId);

        User userWithIdNotInDatabase = userStorage.get(idNotInDatabase);
        List<User> friendsAfterRemove = userStorage.getFriends(idNotInDatabase);

        assertNull(userWithIdNotInDatabase, "Ожидалось null");
        assertEquals(0, friendsAfterRemove.size(), "Список должен быть пустым");
    }

    // удалить из друзей — ID друга нет в базе
    @Test
    void removeFriendWhenFriendIdNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;
        int idNotInDatabase = 3;

        userStorage.add(user);
        userStorage.add(anotherUser);

        userStorage.addFriend(userExpectedId, anotherUserExpectedId);

        userStorage.removeFriend(userExpectedId, idNotInDatabase);

        User userWithIdNotInDatabase = userStorage.get(idNotInDatabase);
        List<User> userFriends = userStorage.getFriends(userExpectedId);

        assertNull(userWithIdNotInDatabase, "Ожидалось null");
        assertEquals(1, userFriends.size(), "Неверное число пользователей в списке");
        assertEquals(anotherUser.getEmail(), userFriends.get(0).getEmail(),
                "В списке нет тот пользователь");
    }

    // удалить из друзей — в базе нет ID пользователя и друга
    @Test
    void removeFriendWhenBothIdsAreNotInDatabase() {
        User user = createUser();
        User anotherUser = createAnotherUser();

        int userExpectedId = 1;
        int anotherUserExpectedId = 2;
        int idNotInDatabase = 3;
        int otherIdNotInDatabase = 4;

        userStorage.add(user);
        userStorage.add(anotherUser);

        userStorage.addFriend(userExpectedId, anotherUserExpectedId);

        userStorage.removeFriend(idNotInDatabase, otherIdNotInDatabase);

        User userWithIdNotInDatabase = userStorage.get(idNotInDatabase);
        List<User> friendsOfUserWithIdNotInDatabase = userStorage.getFriends(idNotInDatabase);

        assertNull(userWithIdNotInDatabase, "Ожидалось null");
        assertEquals(0, friendsOfUserWithIdNotInDatabase.size(),
                "Список должен быть пустой");
    }

    // удалить из друзей — база пустая
    @Test
    void removeFriendFromEmptyDatabase() {
        int someId = 1;
        int someOtherId = 2;

        List<User> users = userStorage.getAll();

        userStorage.removeFriend(someId, someOtherId);

        List<User> someIdUserFriends = userStorage.getFriends(someId);

        assertEquals(0, users.size(), "База должна быть пустой");
        assertEquals(0, someIdUserFriends.size(), "Список должен быть пустым");
    }


    // ---------------------------------------------
    //  ШАБЛОНЫ
    // ---------------------------------------------

    private User createUser() {
        User user = new User();
        user.setEmail("pxl@example.com");
        user.setLogin("pxl2000");
        user.setName("Pixel");
        user.setBirthday(LocalDate.of(2000, Month.JANUARY, 1));
        return user;
    }

    private User createAnotherUser() {
        User user = new User();
        user.setEmail("byte@example.com");
        user.setLogin("iambyte");
        user.setName("Byte");
        user.setBirthday(LocalDate.of(2000, Month.FEBRUARY, 2));
        return user;
    }

    private User createTony() {
        User user = new User();
        user.setEmail("tony@example.com");
        user.setLogin("mynameistony");
        user.setName("Tony");
        user.setBirthday(LocalDate.of(2000, Month.MARCH, 3));
        return user;
    }

    private User createSonya() {
        User user = new User();
        user.setEmail("sonya@example.com");
        user.setLogin("sofie");
        user.setName("Sonya");
        user.setBirthday(LocalDate.of(2000, Month.APRIL, 4));
        return user;
    }
}