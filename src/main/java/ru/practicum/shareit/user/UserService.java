package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mappers.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto createUser(NewUserRequest request) {
        checkEmailExists(request.email());
        User user = userMapper.fromNewRequest(request);
        user.setId(getNextId());
        user = userRepository.create(user);
        log.info("Добавлен новый пользователь \"{}\" c id {}", user.getName(), user.getId());
        return userMapper.toDto(user);
    }

    public UserDto getUserById(long userId) {
        return userMapper.toDto(getUserByIdOrThrow(userId));
    }

    public List<UserDto> getUsers() {
        return userRepository.getUsers().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto updateUser(long userId, UpdateUserRequest request) {
        User user = getUserByIdOrThrow(userId);
        if (request.email() != null) {
            checkEmailExists(request.email());
        }
        userMapper.updateUserFromRequest(user, request);
        user = userRepository.update(user);
        log.info("Обновлен пользователь \"{}\" с id {}", user.getName(), user.getId());
        return userMapper.toDto(user);
    }

    public void deleteUser(long userId) {
        User user = getUserByIdOrThrow(userId);
        userRepository.delete(userId);
        log.info("Удален пользователь \"{}\" с id {}", user.getName(), user.getId());
    }

    private void checkEmailExists(String email) {
        if (userRepository.emailExists(email)) {
            throw new ValidationException("Email already exists: " + email);
        }
    }

    private User getUserByIdOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    private long getNextId() {
        return userRepository.getUsers().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0) + 1;
    }
}
