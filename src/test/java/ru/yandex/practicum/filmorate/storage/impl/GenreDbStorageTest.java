package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class GenreDbStorageTest {
    private final GenreDbStorage genreDbStorage;

    // получить все жанры
    @Test
    void getAllGenres() {
        int expectedGenres = 6;
        List<Genre> genres = genreDbStorage.getAll();
        assertEquals(expectedGenres, genres.size(), "Ожидалось другое количество объектов");
    }

    // получить жанр
    @Test
    void getGenreById() {
        int validId = 1;
        Genre receivedGenre = genreDbStorage.get(validId);
        assertNotNull(receivedGenre, "Ожидалось получить объект Genre");
        assertNotNull(receivedGenre.getName(), "Ожидалось получить название жанра");
    }

    // получить жанр — ID жанра нет в базе
    @Test
    void getGenreByInvalidId() {
        int invalidId = 9;
        Genre receivedGenre = genreDbStorage.get(invalidId);
        assertNull(receivedGenre, "Ожидалось получить null");
    }
}