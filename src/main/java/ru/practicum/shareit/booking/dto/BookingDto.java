package ru.practicum.shareit.booking.dto;

public record BookingDto(
        Long id,
        ItemShortDto item,
        UserShortDto booker,
        String status,
        String start,
        String end
) {
    public record ItemShortDto(Long id, String name) {
    }

    public record UserShortDto(Long id) {
    }
}
