package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("John Doe");
        user.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldCreateValidUser() {
        assertTrue(validator.validate(user).isEmpty());
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("testuser", user.getLogin());
        assertEquals("John Doe", user.getName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthday());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        user.setName("");
        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        user.setEmail("invalid-email");
        assertEquals(1, validator.validate(user).size());
        user.setEmail("inv@lidem@il.ru");
        assertEquals(1, validator.validate(user).size());
    }

    @Test
    void shouldFailWhenLoginHasWhitespaceCharacters() {
        user.setLogin("invalid login");
        assertEquals(1, validator.validate(user).size());
        user.setLogin("invalid  login");
        assertEquals(1, validator.validate(user).size());
    }
}