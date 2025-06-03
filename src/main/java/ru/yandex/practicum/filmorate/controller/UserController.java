package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private static Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody final User user) {
        setNameIfAbsent(user);
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            log.warn("User {} already exists", user);
            throw new DuplicatedDataException("User already exists");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("User {} created", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("User {} not found", newUser);
            throw new ValidationException("Id cannot be null");
        }
        setNameIfAbsent(newUser);
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (!oldUser.getEmail().equals(newUser.getEmail())
                    && users.values().stream().anyMatch(u -> u.getEmail().equals(newUser.getEmail()))) {
                log.warn("User {} already exists", newUser);
                throw new DuplicatedDataException("User already exists");
            }

            users.replace(newUser.getId(), newUser);
            log.info("User {} updated", newUser);
            return newUser;
        } else {
            log.warn("User {} not found", newUser);
            throw new NotFoundException(String.format("User with id '%s' not found", newUser.getId()));
        }
    }

    private void setNameIfAbsent(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Set name {} for user {}", user.getName(), user);
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
