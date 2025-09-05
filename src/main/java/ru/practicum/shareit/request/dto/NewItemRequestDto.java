package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;

public record NewItemRequestDto(
        @NotBlank(message = "Описание не может быть пустым")
        String description
) {
}