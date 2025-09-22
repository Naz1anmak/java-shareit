package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserRepository userRepository;

    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        existingUser = userRepository.save(new User("Existing User", "existing@test.com"));
    }

    @Test
    void createUser_shouldCreateUser() {
        NewUserRequest request = new NewUserRequest("John Doe", "john.doe@test.com");

        UserDto result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isPositive();
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john.doe@test.com");

        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getEmail)
                .contains("john.doe@test.com");
    }

    @Test
    void createUser_shouldThrowWhenEmailAlreadyExists() {
        NewUserRequest request = new NewUserRequest("Another User", "existing@test.com");

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Email already exists: existing@test.com");
    }

    @Test
    void getUserById_shouldReturnUser() {
        UserDto result = userService.getUserById(existingUser.getId());

        assertThat(result.id()).isEqualTo(existingUser.getId());
        assertThat(result.name()).isEqualTo("Existing User");
        assertThat(result.email()).isEqualTo("existing@test.com");
    }

    @Test
    void getUserById_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with ID: 999");
    }

    @Test
    void getUsers_shouldReturnAllUsers() {
        User anotherUser = userRepository.save(new User("Another User", "another@test.com"));

        List<UserDto> result = userService.getUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::email)
                .containsExactlyInAnyOrder("existing@test.com", "another@test.com");
    }

    @Test
    void getUsers_shouldReturnEmptyListWhenNoUsers() {
        userRepository.deleteAll();

        List<UserDto> result = userService.getUsers();

        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_shouldUpdateAllFields() {
        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "updated@test.com");

        UserDto result = userService.updateUser(existingUser.getId(), request);

        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.email()).isEqualTo("updated@test.com");

        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void updateUser_shouldUpdatePartialFields() {
        UpdateUserRequest nameOnlyRequest = new UpdateUserRequest("New Name", null);
        UserDto nameResult = userService.updateUser(existingUser.getId(), nameOnlyRequest);

        assertThat(nameResult.name()).isEqualTo("New Name");
        assertThat(nameResult.email()).isEqualTo("existing@test.com");

        UpdateUserRequest emailOnlyRequest = new UpdateUserRequest(null, "newemail@test.com");
        UserDto emailResult = userService.updateUser(existingUser.getId(), emailOnlyRequest);

        assertThat(emailResult.name()).isEqualTo("New Name");
        assertThat(emailResult.email()).isEqualTo("newemail@test.com");
    }

    @Test
    void updateUser_shouldThrowWhenEmailAlreadyExists() {
        User anotherUser = userRepository.save(new User("Another", "another@test.com"));

        UpdateUserRequest request = new UpdateUserRequest("Name", "another@test.com");

        assertThatThrownBy(() -> userService.updateUser(existingUser.getId(), request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Email already exists: another@test.com");
    }

    @Test
    void updateUser_shouldNotThrowWhenEmailNotChanged() {
        UpdateUserRequest request = new UpdateUserRequest("New Name", "existing@test.com");

        assertThatCode(() -> userService.updateUser(existingUser.getId(), request))
                .doesNotThrowAnyException();

        UserDto result = userService.updateUser(existingUser.getId(), request);
        assertThat(result.email()).isEqualTo("existing@test.com");
        assertThat(result.name()).isEqualTo("New Name");
    }

    @Test
    void updateUser_shouldThrowWhenUserNotFound() {
        UpdateUserRequest request = new UpdateUserRequest("Name", "email@test.com");

        assertThatThrownBy(() -> userService.updateUser(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with ID: 999");
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        userService.deleteUser(existingUser.getId());

        assertThat(userRepository.existsById(existingUser.getId())).isFalse();

        assertThatThrownBy(() -> userService.getUserById(existingUser.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteUser_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with ID: 999");
    }

    @Test
    void getUserByIdOrThrow_shouldReturnUser() {
        User result = userService.getUserByIdOrThrow(existingUser.getId());

        assertThat(result.getId()).isEqualTo(existingUser.getId());
        assertThat(result.getName()).isEqualTo("Existing User");
        assertThat(result.getEmail()).isEqualTo("existing@test.com");
    }

    @Test
    void getUserByIdOrThrow_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getUserByIdOrThrow(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with ID: 999");
    }

    @Test
    void updateUser_shouldWorkWithSameEmailDifferentCase() {
        UpdateUserRequest request = new UpdateUserRequest("Name", "EXISTING@test.com");

        try {
            UserDto result = userService.updateUser(existingUser.getId(), request);
            assertThat(result.email()).isEqualTo("EXISTING@test.com");
        } catch (ValidationException e) {
            assertThat(e.getMessage()).contains("Email already exists");
        }
    }

    @Test
    void createUser_shouldHandleLongNamesAndEmails() {
        String longName = "A".repeat(255);
        String longEmail = "test@" + "a".repeat(240) + ".com";

        if (longEmail.length() <= 255) {
            NewUserRequest request = new NewUserRequest(longName, longEmail);

            UserDto result = userService.createUser(request);

            assertThat(result.name()).isEqualTo(longName);
            assertThat(result.email()).isEqualTo(longEmail);
        }
    }

    @Test
    void getUsers_shouldReturnUsersInCorrectOrder() {
        User user1 = userRepository.save(new User("User1", "user1@test.com"));
        User user2 = userRepository.save(new User("User2", "user2@test.com"));

        List<UserDto> result = userService.getUsers();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(UserDto::email)
                .contains("existing@test.com", "user1@test.com", "user2@test.com");
    }
}