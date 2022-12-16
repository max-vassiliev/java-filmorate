package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;


    // добавить и получить фильм
    @Test
    void addFilm() {
        int expectedId = 1;
        Film film = createFilm();

        filmStorage.add(film);
        Film savedFilm = filmStorage.get(expectedId);

        assertEquals(film.getName(), savedFilm.getName(), "Не совпадают поля name");
        assertEquals(film.getDescription(),savedFilm.getDescription(), "Не совпадают поля description");
        assertEquals(film.getReleaseDate(), savedFilm.getReleaseDate(), "Не совпадают поля releaseDate");
        assertEquals(film.getDuration(), savedFilm.getDuration(), "Не совпадают поля duration");
        assertEquals(film.getMpa().getId(), savedFilm.getMpa().getId(), "Не совпадают id объекта mpa");
    }

    // получить фильм — ID нет в базе
    @Test
    void getFilmWithIdNotInTheDatabase() {
        int idNotInDatabase = 2;
        Film film = createFilm();
        filmStorage.add(film);

        Film savedFilm = filmStorage.get(idNotInDatabase);
        assertNull(savedFilm, "Ожидалось null");
    }

    // добавить фильм — база пустая
    @Test
    void getFilmFromEmptyDatabase() {
        int randomId = 1;
        Film film = filmStorage.get(randomId);
        assertNull(film, "Ожидалось null");
    }

    // получить все фильмы
    @Test
    void getAllFilms() {
        Film film = createFilm();
        Film anotherFilm = createFilm2();
        filmStorage.add(film);
        filmStorage.add(anotherFilm);

        List<Film> films = filmStorage.getAll();
        assertEquals(2, films.size(), "Ожидалось другое количество фильмов");
        assertEquals(film.getName(), films.get(0).getName(), "Ожидался фильм с другим названием");
        assertEquals(anotherFilm.getName(), films.get(1).getName(), "Ожидался фильм с другим названием");
    }

    // получить все фильмы — база пустая
    @Test
    void getAllFilmsFromEmptyDatabase() {
        List<Film> films = filmStorage.getAll();
        assertEquals(0, films.size(), "Список должен быть пустым");
    }

    // обновить данные о фильме
    @Test
    void updateFilm() {
        Film film = createFilm();

        filmStorage.add(film);

        Film filmToUpdate = createFilm2();
        filmToUpdate.setId(film.getId());

        filmStorage.update(filmToUpdate);

        Film filmUpdated = filmStorage.get(filmToUpdate.getId());

        assertEquals(film.getId(), filmUpdated.getId(), "ID фильмов не совпадают");
        assertNotEquals(film.getName(), filmUpdated.getName(), "Названия не должны совпадать");
        assertEquals(filmToUpdate.getName(), filmUpdated.getName(), "Названия не совпадают");
    }

    // обновить данные о фильме — ID нет в базе
    @Test
    void updateFilmWithIdNotInDatabase() {
        int idNotInDatabase = 2;
        Film film = createFilm();

        filmStorage.add(film);

        Film filmToUpdate = createFilm2();
        filmToUpdate.setId(idNotInDatabase);

        assertThrows(Throwable.class, () -> filmStorage.update(filmToUpdate));
    }

    // добавить лайк
    @Test
    void addLike() {
        Film film = createFilm();
        User user = createUser();

        filmStorage.add(film);
        userStorage.add(user);
        filmStorage.addLike(film.getId(), user.getId());

        Film savedFilm = filmStorage.get(film.getId());
        User savedUser = userStorage.get(user.getId());
        List<Integer> savedFilmLikes = new ArrayList<>(savedFilm.getLikes());

        assertEquals(1, savedFilmLikes.size(), "Неверное количество лайков");
        assertEquals(savedUser.getId(), savedFilmLikes.get(0), "Сохранен не тот ID пользователя");
    }

    // добавить лайк — повторный лайк от пользователя
    @Test
    void addLikeFromSameUser() {
        Film film = createFilm();
        User user = createUser();

        filmStorage.add(film);
        userStorage.add(user);
        filmStorage.addLike(film.getId(), user.getId());

        Film filmAfterLike1 = filmStorage.get(film.getId());
        List<Integer> savedLikes1 = new ArrayList<>(filmAfterLike1.getLikes());

        filmStorage.addLike(film.getId(), user.getId());

        Film filmAfterLike2 = filmStorage.get(film.getId());
        List<Integer> savedLikes2 = new ArrayList<>(filmAfterLike2.getLikes());

        assertEquals(savedLikes1.size(), savedLikes2.size(), "Неверное количество лайков у фильма");
        assertEquals(user.getId(), savedLikes1.get(0), "Сохранен неверный ID пользователя");
        assertEquals(user.getId(), savedLikes2.get(0), "Сохранен неверный ID пользователя");
    }

    // добавить лайк — ID фильма нет в базе
    @Test
    void addLikeWhenFilmIdNotInDatabase() {
        int invalidFilmId = 2;
        Film film = createFilm();
        User user = createUser();
        filmStorage.add(film);
        userStorage.add(user);

        assertThrows(Throwable.class, () -> filmStorage.addLike(invalidFilmId, user.getId()));
    }

    // добавить лайк — ID пользователя нет в базе
    @Test
    void addLikeWhenUserIdNotInDatabase() {
        int invalidUserId = 2;
        Film film = createFilm();
        User user = createUser();
        filmStorage.add(film);
        userStorage.add(user);

        assertThrows(Throwable.class, () -> filmStorage.addLike(film.getId(), invalidUserId));
    }

    // добавить лайк — ID фильма и пользователя нет в базе
    @Test
    void addLikeWhenBothIdsAreNotInDatabase() {
        int invalidId = 2;
        Film film = createFilm();
        User user = createUser();
        filmStorage.add(film);
        userStorage.add(user);

        assertThrows(Throwable.class, () -> filmStorage.addLike(invalidId, invalidId));
    }

    // добавить лайк — база пустая
    @Test
    void addLikeWhenDatabaseIsEmpty() {
        int invalidId = 1;
        assertThrows(Throwable.class, () -> filmStorage.addLike(invalidId, invalidId));
    }

    // удалить лайк
    @Test
    void removeLike() {
        Film film = createFilm();
        User user = createUser();

        filmStorage.add(film);
        userStorage.add(user);
        filmStorage.addLike(film.getId(), user.getId());

        Film likedFilm = filmStorage.get(film.getId());

        filmStorage.removeLike(film.getId(), user.getId());

        Film dislikedFilm = filmStorage.get(film.getId());

        assertNotEquals(likedFilm.getLikes().size(), dislikedFilm.getLikes().size(),
                "Количество лайков не должно совпадать");
        assertEquals(0, dislikedFilm.getLikes().size(),
                "У фильма не должно быть лайков");
    }

    // удалить лайк, которого не было
    @Test
    void removeNonExistingLike() {
        Film film = createFilm();
        User user = createUser();
        filmStorage.add(film);
        userStorage.add(user);

        filmStorage.removeLike(film.getId(), user.getId());
        List<Integer> likes = new ArrayList<>(film.getLikes());

        assertEquals(0, likes.size(), "Неверное количество лайков");
    }

    // удалить лайк — ID фильма нет в базе
    @Test
    void removeLikeWhenFilmIdNotInDatabase() {
        int invalidId = 5;
        Film film = createFilm();
        User user = createUser();
        filmStorage.add(film);
        userStorage.add(user);

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(invalidId, user.getId());

        Film savedFilm = filmStorage.get(film.getId());
        List<Integer> likes = new ArrayList<>(savedFilm.getLikes());

        assertEquals(1, likes.size(), "Неверное количество лайков у фильма");
        assertEquals(user.getId(), likes.get(0), "Сохранен неверный ID пользователя");
    }

    // удалить лайк — ID пользователя нет в базе
    @Test
    void removeLikeWhenUserIdNotInDatabase() {
        int invalidId = 5;
        Film film = createFilm();
        User user = createUser();
        filmStorage.add(film);
        userStorage.add(user);

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), invalidId);

        Film savedFilm = filmStorage.get(film.getId());
        List<Integer> likes = new ArrayList<>(savedFilm.getLikes());

        assertEquals(1, likes.size(), "Неверное количество лайков у фильма");
        assertEquals(user.getId(), likes.get(0), "Сохранен неверный ID пользователя");
    }

    // удалить лайк — ID фильма и пользователя нет в базе
    @Test
    void removeLikeWhenBothIdsAreNotInDatabase() {
        int invalidId = 5;
        Film film = createFilm();
        User user = createUser();
        filmStorage.add(film);
        userStorage.add(user);
        filmStorage.addLike(film.getId(), user.getId());

        filmStorage.removeLike(invalidId, invalidId);

        Film savedFilm = filmStorage.get(film.getId());
        List<Integer> likes = new ArrayList<>(savedFilm.getLikes());

        assertEquals(1, likes.size(), "Неверное количество лайков у фильма");
        assertEquals(user.getId(), likes.get(0), "Сохранен неверный ID пользователя");
    }

    // удалить лайк — база пустая
    @Test
    void removeLikeWhenDatabaseIsEmpty() {
        int invalidId = 5;
        List<Film> films = filmStorage.getAll();
        List<User> users = userStorage.getAll();

        filmStorage.removeLike(invalidId, invalidId);

        assertEquals(0, films.size(), "В базе не должно быть фильмов");
        assertEquals(0, users.size(), "В базе не должно быть пользователей");
    }

    // получить список популярных фильмов
    @Test
    void getTopFilms() {
        int defaultLimit = 10;
        generateTopFilms(filmStorage, userStorage);
        List<Film> topFilms = filmStorage.getTopFilms(defaultLimit);
        assertEquals(4, topFilms.size(), "Неверное количество фильмов");
    }

    // получить список популярных фильмов — задано ограничение по размеру
    @Test
    void getTopFilmsWithSetLimit() {
        int limit = 3;
        generateTopFilms(filmStorage, userStorage);
        List<Film> films = filmStorage.getAll();
        List<Film> topFilms = filmStorage.getTopFilms(limit);

        assertEquals(limit, topFilms.size(), "Возвращено неверное количество популярных фильмов");
        assertNotEquals(films.size(), topFilms.size(), "Списки фильмов не должны совпадать");
    }

    // получить список популярных фильмов — пустая база
    @Test
    void getTopFilmsFromEmptyDatabase() {
        int defaultLimit = 10;
        List<Film> films = filmStorage.getAll();
        List<Film> topFilms = filmStorage.getTopFilms(defaultLimit);

        assertEquals(0, topFilms.size(), "Список должен быть пустым");
        assertEquals(films.size(), topFilms.size(), "Списки фильмов должны совпадать");
    }


    // ---------------------------------------------
    //  ШАБЛОНЫ
    // ---------------------------------------------

    private Film createFilm() {
        Film film = new Film();
        film.setId(1);
        film.setName("Movie");
        film.setDescription("Movie description");
        film.setReleaseDate(LocalDate.of(2000, Month.DECEMBER, 28));
        film.setDuration(100);

        film.setMpa(new Mpa());
        film.getMpa().setId(1);

        film.getGenres().add(createGenre1());

        return film;
    }

    private Film createFilm2() {
        Film film = new Film();
        film.setId(2);
        film.setName("MotionPicture");
        film.setDescription("Motion Picture description");
        film.setReleaseDate(LocalDate.of(2010, Month.JUNE, 21));
        film.setDuration(200);

        film.setMpa(new Mpa());
        film.getMpa().setId(2);

        film.getGenres().add(createGenre2());
        film.getGenres().add(createGenre3());

        return film;
    }

    private Film createFilm3() {
        Film film = new Film();
        film.setId(3);
        film.setName("Film");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(1950, Month.OCTOBER, 15));
        film.setDuration(120);

        film.setMpa(new Mpa());
        film.getMpa().setId(2);

        film.getGenres().add(createGenre3());
        film.getGenres().add(createGenre2());
        film.getGenres().add(createGenre1());

        return film;
    }

    private Film createFilm4() {
        Film film = new Film();
        film.setId(4);
        film.setName("Video");
        film.setDescription("Video description");
        film.setReleaseDate(LocalDate.of(1950, Month.APRIL, 7));
        film.setDuration(200);

        film.setMpa(new Mpa());
        film.getMpa().setId(3);

        film.getGenres().add(createGenre3());

        return film;
    }

    private Genre createGenre1() {
        Genre genre = new Genre();
        genre.setId(1);
        return genre;
    }

    private Genre createGenre2() {
        Genre genre = new Genre();
        genre.setId(2);
        return genre;
    }

    private Genre createGenre3() {
        Genre genre = new Genre();
        genre.setId(3);
        return genre;
    }

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("pxl@example.com");
        user.setLogin("pxl2000");
        user.setName("Pixel");
        user.setBirthday(LocalDate.of(2000, Month.JANUARY, 1));
        return user;
    }

    private User createAnotherUser() {
        User user = new User();
        user.setId(2);
        user.setEmail("byte@example.com");
        user.setLogin("iambyte");
        user.setName("Byte");
        user.setBirthday(LocalDate.of(2000, Month.FEBRUARY, 2));
        return user;
    }

    private User createTony() {
        User user = new User();
        user.setId(3);
        user.setEmail("tony@example.com");
        user.setLogin("mynameistony");
        user.setName("Tony");
        user.setBirthday(LocalDate.of(2000, Month.MARCH, 3));
        return user;
    }

    private User createSonya() {
        User user = new User();
        user.setId(4);
        user.setEmail("sonya@example.com");
        user.setLogin("sofie");
        user.setName("Sonya");
        user.setBirthday(LocalDate.of(2000, Month.APRIL, 4));
        return user;
    }

    private void generateTopFilms(FilmStorage filmStorage, UserStorage userStorage) {
        Film film1 = createFilm();
        Film film2 = createFilm2();
        Film film3 = createFilm3();
        Film film4 = createFilm4();
        User user1 = createUser();
        User user2 = createAnotherUser();
        User user3 = createTony();
        User user4 = createSonya();

        filmStorage.add(film1);
        filmStorage.add(film2);
        filmStorage.add(film3);
        filmStorage.add(film4);
        userStorage.add(user1);
        userStorage.add(user2);
        userStorage.add(user3);
        userStorage.add(user4);

        // add likes to film1
        filmStorage.addLike(film1.getId(), user1.getId());

        // add likes to film2
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());

        // add likes to film3
        filmStorage.addLike(film3.getId(), user1.getId());
        filmStorage.addLike(film3.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user3.getId());

        // add likes to film4
        filmStorage.addLike(film4.getId(), user1.getId());
        filmStorage.addLike(film4.getId(), user2.getId());
        filmStorage.addLike(film4.getId(), user3.getId());
        filmStorage.addLike(film4.getId(), user4.getId());
    }

}