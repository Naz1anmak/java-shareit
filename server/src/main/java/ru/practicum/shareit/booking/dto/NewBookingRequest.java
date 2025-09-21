package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public record NewBookingRequest(
        Long itemId,
        LocalDateTime start,
        LocalDateTime end) {
}
