package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findById(long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found: " + userId);
        }
        return user;
    }

    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    public User create(final User user) {
        return userRepository.create(user);
    }

    public User update(User newUser) {
        return userRepository.update(newUser);
    }

    public void addFriend(Long userId, Long friendUserId) {
        if (userId == null || friendUserId == null)
            throw new ValidationException("userId or friendUserId cannot be null");
        if (userId.equals(friendUserId))
            throw new ValidationException("User cannot be a friend of himself");

        User user = userRepository.findById(userId);
        User friendUser = userRepository.findById(friendUserId);
        if (user == null || friendUser == null)
            throw new NotFoundException("user cannot be null");
        userRepository.addFriend(userId, friendUserId);
    }

    public void removeFriend(Long userId, Long friendUserId) {
        if (userId == null || friendUserId == null)
            throw new NotFoundException("userId or friendUserId cannot be null");
        if (userId.equals(friendUserId))
            throw new NotFoundException("User can't remove himself from friends");
        if (!exists(userId) || !exists(friendUserId))
            throw new NotFoundException("user not found");
        userRepository.removeFriend(userId, friendUserId);
    }

    public Collection<User> getFriends(Long userId) {
        if (userId == null) {
            throw new ValidationException("userId cannot be null");
        }
        if (!exists(userId)) {
            throw new NotFoundException("user not found");
        }
        return userRepository.getFriends(userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        if (userId == null || otherUserId == null)
            throw new ValidationException("userId or otherUserId cannot be null");
        if (userId.equals(otherUserId))
            throw new ValidationException("User cannot be a friend of himself");

        return userRepository.getCommonFriends(userId, otherUserId);
    }

    public boolean exists(long userId) {
        return userRepository.findById(userId) != null;
    }

    public void delete(long userId) {
        if (!exists(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
