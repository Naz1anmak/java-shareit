package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NewUserRequest(
        @NotBlank(message = "Имя не должно быть пустым")
        String name,
        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный формат email")
        String email) {
}
