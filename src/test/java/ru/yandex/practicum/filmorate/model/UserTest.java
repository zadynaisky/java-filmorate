package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testValidUserCreation() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("John Doe");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("testuser", user.getLogin());
        assertEquals("John Doe", user.getName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthday());
    }

    @Test
    void testEmailValidation() {
        User user = new User();

        // Проверка на null
        assertThrows(ConstraintViolationException.class, () -> {
            user.setEmail(null);
        });

        // Проверка на некорректный email
        assertThrows(ConstraintViolationException.class, () -> {
            user.setEmail("incorrectemail");
        });

        // Проверка на пустой email
        assertThrows(ConstraintViolationException.class, () -> {
            user.setEmail("");
        });

        // Проверка на валидный email
        user.setEmail("test@example.com");
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void testLoginValidation() {
        User user = new User();

        // Проверка на null
        assertThrows(ConstraintViolationException.class, () -> {
            user.setLogin(null);
        });

        // Проверка на пустую строку
        assertThrows(ConstraintViolationException.class, () -> {
            user.setLogin("");
        });

        // Проверка на логин с пробелами
        assertThrows(ConstraintViolationException.class, () -> {
            user.setLogin("test user");
        });

        // Проверка на валидный логин
        user.setLogin("testuser");
        assertEquals("testuser", user.getLogin());
    }

    @Test
    void testBirthdayValidation() {
        User user = new User();

        // Проверка на будущее
        assertThrows(ConstraintViolationException.class, () -> {
            user.setBirthday(LocalDate.now().plusDays(1));
        });

        // Проверка на валидную дату в прошлом
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthday());
    }

    @Test
    void testNameValidation() {
        User user = new User();

        // Проверка на null (необязательное поле)
        user.setName(null);
        assertNull(user.getName());

        // Проверка на пустую строку (необязательное поле)
        user.setName("");
        assertEquals("", user.getName());

        // Проверка на валидное имя
        user.setName("John Doe");
        assertEquals("John Doe", user.getName());
    }
}