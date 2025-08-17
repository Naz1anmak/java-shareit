package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    public User create(User user) {
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public User update(User user) {
        if (!emails.contains(user.getEmail())) {
            emails.remove(users.get(user.getId()).getEmail());
            emails.add(user.getEmail());
        }
        users.put(user.getId(), user);
        return user;
    }

    public void delete(long userId) {
        users.remove(userId);
    }

    public boolean emailExists(String email) {
        return emails.contains(email);
    }
}
