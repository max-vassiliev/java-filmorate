package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Repository("usersDb")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    // получить пользователя
    @Override
    public User get(int id) {
        final String sqlQuery = "select USER_ID, EMAIL, LOGIN, USER_NAME, BIRTHDAY " +
                "from USERS where USER_ID = ?";
        final List<User> users = jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser, id);

        if (users.isEmpty()) {
            return null;
        }

        User user = users.get(0);
        loadUserFriends(user);
        return user;
    }

    // получить всех пользователей
    @Override
    public List<User> getAll() {
        final String sqlQuery = "select USER_ID, EMAIL, LOGIN, USER_NAME, BIRTHDAY from USERS";
        final List<User> users = jdbcTemplate.query(sqlQuery, UserDbStorage::makeUser);

        users.forEach(this::loadUserFriends);
        return users;
    }

    // добавить пользователя
    @Override
    public void add(User user) {
        final String sqlQuery = "insert into USERS (EMAIL, LOGIN, USER_NAME, BIRTHDAY) " +
                "values (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"USER_ID"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            final LocalDate birthday = user.getBirthday();
            if (birthday == null) {
                stmt.setNull(4, Types.NULL);
            } else {
                stmt.setDate(4, Date.valueOf(birthday));
            }
            return stmt;
        }, keyHolder);
    }

    // обновить данные о пользователе
    @Override
    public void update(User user) {
        final String sqlQuery = "update USERS " +
                "set EMAIL = ?, LOGIN = ?, USER_NAME = ?, BIRTHDAY = ? " +
                "where USER_ID = ?";

        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
    }

    // получить друзей пользователя
    @Override
    public List<User> getFriends(int userId) {
        final String sql = "select USER_ID, EMAIL, LOGIN, USER_NAME, BIRTHDAY " +
                "from USERS " +
                "where USER_ID " +
                "in (select FRIEND_ID from FRIENDS where USER_ID = ?)";
        List<User> friends = jdbcTemplate.query(sql, UserDbStorage::makeUser, userId);

        friends.forEach(this::loadUserFriends);
        return friends;
    }

    // получить список общих друзей
    @Override
    public List<User> getCommonFriends(int idUser, int idOtherUser) {
        final String sql = "select u.USER_ID, u.EMAIL, u.LOGIN, u.USER_NAME, u.BIRTHDAY " +
                "from USERS u, FRIENDS f1, FRIENDS f2 " +
                "where f1.USER_ID = ? " +
                "and f2.USER_ID = ? " +
                "and f1.FRIEND_ID = f2.FRIEND_ID " +
                "and f1.FRIEND_ID = u.USER_ID";

        List<User> friends = jdbcTemplate.query(sql, UserDbStorage::makeUser, idUser, idOtherUser);

        friends.forEach(this::loadUserFriends);
        return friends;
    }

    // добавить дружбу в базу
    @Override
    public void addFriend(int userId, int friendId) {
        final String sqlQuery = "merge into FRIENDS (USER_ID, FRIEND_ID) values (?, ?)";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    // удалить дружбу из базы
    @Override
    public void removeFriend(int userId, int friendId) {
        final String sqlQuery = "delete from FRIENDS where USER_ID = ? and FRIEND_ID = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }


    // ---------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ---------------------------------------------

    // создать объект пользователя (User)
    static User makeUser(ResultSet rs, int id) throws SQLException {
        User user = new User(rs.getInt("USER_ID"),
                rs.getString("EMAIL"),
                rs.getString("LOGIN"),
                rs.getString("USER_NAME")
        );
        if (rs.getDate("BIRTHDAY") != null) {
            user.setBirthday(rs.getDate("BIRTHDAY").toLocalDate());
        }
        return user;
    }

    // выгрузить из базы список ID друзей и добавить пользователю
    private void loadUserFriends(User user) {
        final String sqlQuery = "select * from FRIENDS where USER_ID = ?";
        List<Integer> friends = jdbcTemplate.query(sqlQuery,
                (rs, rowNum) -> rs.getInt("FRIEND_ID"), user.getId());
        user.setFriends(new HashSet<>(friends));
    }
}
