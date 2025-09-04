package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

public record BookingForItemDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        BookingStatus status,
        Long itemId,
        String itemName,
        Long bookerId
) {}
