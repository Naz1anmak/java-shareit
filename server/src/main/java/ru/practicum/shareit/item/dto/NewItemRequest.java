package ru.practicum.shareit.item.dto;

public record NewItemRequest(
        String name,
        String description,
        Boolean available,
        Long requestId) {
}
