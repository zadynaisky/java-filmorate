package ru.yandex.practicum.filmorate.storage.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.util.Collection;

@Repository
@Slf4j
public class UserRepository extends BaseRepository<User> implements UserStorage {
    private final UserRowMapper userRowMapper;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.userRowMapper = new UserRowMapper();
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT * FROM `user` where id = ?;";
        return findOne(sql, userRowMapper, id);
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM `user`;";
        return findMany(sql, userRowMapper);
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO `user` (email, login, name, birthdate) VALUES (?, ?, ?, ?);";
        long userId = insert(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(userId);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE `user` SET email = ?, login = ?, name = ?, birthdate = ? WHERE id = ?;";
        update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    public void addFriend(Long userId, Long friendUserId) {
        String sql = "INSERT INTO friend (user_id, friend_id) VALUES (?, ?);";
        update(sql, userId, friendUserId); // Используйте update вместо insertMultipleKeys
    }

    public void removeFriend(Long userId, Long friendUserId) {
        String sql = "DELETE FROM friend WHERE user_id = ? AND friend_id = ?;";
        delete(sql, userId, friendUserId);
    }

    public Collection<User> getFriends(Long userId) {
        String sql = "SELECT * FROM `user` WHERE id IN (SELECT friend_id FROM friend WHERE user_id = ?);";
        return findMany(sql, userRowMapper, userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        String sql = """
                SELECT * FROM `user` WHERE id IN (
                    SELECT friend_id
                    FROM friend WHERE user_id = ?
                    INTERSECT
                    SELECT friend_id
                    FROM friend WHERE user_id = ?);
                """;
        return findMany(sql, userRowMapper, userId, otherUserId);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM \"USER\" WHERE id = ?";
        delete(sql, id);
    }

}