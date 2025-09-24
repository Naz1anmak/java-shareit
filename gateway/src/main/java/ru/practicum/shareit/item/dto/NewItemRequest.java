package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewItemRequest(
        @NotBlank(message = "Название вещи не должно быть пустым")
        String name,

        @NotBlank(message = "Описание вещи не должно быть пустым")
        @Size(max = 200, message = "Максимальная длина описания — 200 символов")
        String description,

        @NotNull(message = "Укажите статус доступа к аренде")
        Boolean available,

        Long requestId) {
}
