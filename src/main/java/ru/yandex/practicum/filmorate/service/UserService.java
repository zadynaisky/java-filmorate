package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserService {
    private final InMemoryUserStorage userStorage;
    private final UserRepository userRepository;

    public User findById(long userId) {
        return userRepository.findById(userId);
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
                throw new ValidationException("user not found");

        //userStorage.findById(userId).getFriends().add(friendUserId);
        //userStorage.findById(friendUserId).getFriends().add(userId);
    }

    public void removeFriend(Long userId, Long friendUserId) {
        if (userId == null || friendUserId == null)
            throw new ValidationException("userId or friendUserId cannot be null");
        if (userId.equals(friendUserId))
            throw new ValidationException("User can't remove himself from friends");
        userStorage.findById(userId).getFriends().remove(friendUserId);
        userStorage.findById(friendUserId).getFriends().remove(userId);
    }

    public List<User> getFriends(Long userId) {
        if (userId == null)
            throw new ValidationException("userId cannot be null");
        return userStorage
                .findById(userId)
                .getFriends()
                .stream()
                .map(userStorage::findById)
                .sorted()
                .collect(toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        if (userId == null || otherUserId == null)
            throw new ValidationException("userId or otherUserId cannot be null");
        if (userId.equals(otherUserId))
            throw new ValidationException("User cannot be a friend of himself");
        Set<Long> otherUserFriends = userStorage.findById(otherUserId).getFriends();

        return userStorage
                .findById(userId)
                .getFriends()
                .stream()
                .filter(x -> otherUserFriends.contains(x))
                .map(userStorage::findById)
                .sorted()
                .collect(toList());
    }
}
