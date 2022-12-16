package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MpaDbStorageTest {

    private final MpaDbStorage mpaDbStorage;

    // получить все рейтинги
    @Test
    void getAllMpa() {
        int expectedMpa = 5;
        List<Mpa> mpaList = mpaDbStorage.getAll();
        assertEquals(expectedMpa, mpaList.size(), "Ожидалось другое количество объектов");
        assertNotNull(mpaList.get(0).getName(), "Ожидалось название рейтинга");
    }

    // получить рейтинг
    @Test
    void getMpaById() {
        int validId = 1;
        Mpa receivedMpa = mpaDbStorage.get(validId);
        assertNotNull(receivedMpa, "Ожидалось получить объект Mpa");
        assertNotNull(receivedMpa.getName(), "Ожидалось название рейтинга");
    }

    // получить рейтинг — ID рейтинга нет в базе
    @Test
    void getMpaByInvalidId() {
        int invalidId = 9;
        Mpa receivedMpa = mpaDbStorage.get(invalidId);
        assertNull(receivedMpa, "Ожидалось получить null");
    }
}