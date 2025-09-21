package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewCommentRequest(
        @NotBlank(message = "Отзыв не должен быть пустым")
        @Size(max = 1000, message = "Максимальная длина отзыва — 1000 символов")
        String text) {
}
