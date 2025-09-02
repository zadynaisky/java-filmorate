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
        //String sql = "INSERT INTO friend (user_id, friend_id) VALUES (?, ?);";

        String sql = """
            MERGE INTO friend AS f
            USING (VALUES (?, ?))
            AS s (user_id, friend_id)
            ON (f.user_id = s.user_id AND f.friend_id = s.friend_id)
            WHEN MATCHED THEN 
                UPDATE SET 
                    o.quantity = s.quantity,
                    o.price = s.price
            WHEN NOT MATCHED THEN 
                INSERT (product_id, customer_id, quantity, price) 
                VALUES (s.product_id, s.customer_id, s.quantity, s.price)
            """;
        new JdbcTemplate().update(sql, userId, friendUserId);
        //insert(sql, userId, friendUserId);
    }

    public void removeFriend(Long userId, Long friendUserId) {
        String sql = "DELETE FROM friend WHERE user_id = ? AND friend_id = ?;";
        delete(sql, userId, friendUserId);
    }
}
