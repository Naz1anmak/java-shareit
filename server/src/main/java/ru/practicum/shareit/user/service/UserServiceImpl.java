package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        checkEmailExists(request.email());
        User user = userMapper.fromNewRequest(request);
        user = userRepository.save(user);
        log.info("Добавлен новый пользователь \"{}\" c id {}", user.getName(), user.getId());
        return userMapper.toDto(user);
    }

    @Override
    public UserDto getUserById(long userId) {
        return userMapper.toDto(getUserByIdOrThrow(userId));
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto updateUser(long userId, UpdateUserRequest request) {
        User user = getUserByIdOrThrow(userId);
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            checkEmailExists(request.email());
        }
        userMapper.updateUserFromRequest(user, request);
        user = userRepository.save(user);
        log.info("Обновлен пользователь \"{}\" с id {}", user.getName(), user.getId());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        User user = getUserByIdOrThrow(userId);
        userRepository.delete(user);
        log.info("Удален пользователь \"{}\" с id {}", user.getName(), user.getId());
    }

    @Override
    public User getUserByIdOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    private void checkEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Email already exists: " + email);
        }
    }
}
