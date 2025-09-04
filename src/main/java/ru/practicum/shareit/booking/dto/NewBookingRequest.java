package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NewBookingRequest(
        @NotNull(message = "Id вещи не должен быть пустым")
        Long itemId,

        @NotNull(message = "Укажите дату и время начала бронирования")
        @Future(message = "Дата начала бронирования должна быть в будущем")
        LocalDateTime start,

        @NotNull(message = "Укажите дату и время окончания бронирования")
        @Future(message = "Дата окончания бронирования должна быть в будущем")
        LocalDateTime end) {
}
