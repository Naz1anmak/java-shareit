package ru.practicum.shareit.user.mappers;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

public class UserMapper {
    public static User mapToUser(NewUserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return user;
    }

    public static UserDto mapToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static void updateUserFields(User user, UpdateUserRequest request) {
        if (request.hasName()) {
            user.setName(request.name());
        }
        if (request.hasEmail()) {
            user.setEmail(request.email());
        }
    }
}
