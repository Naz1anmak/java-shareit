package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();

    public User create(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public void delete(long userId) {
        users.remove(userId);
    }
}
