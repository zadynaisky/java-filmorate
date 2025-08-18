package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User findById(Long id) {
        if (id == null) {
            throw new ValidationException("Id cannot be null");
        }
        if (!users.containsKey(id)) {
            log.warn("User with id={} not found", id);
            throw new NotFoundException(String.format("User with id '%s' not found", id));
        }
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(final User user) {
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

    @Override
    public User update(User newUser) {
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
