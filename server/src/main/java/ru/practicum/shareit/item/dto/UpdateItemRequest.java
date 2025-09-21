package ru.practicum.shareit.item.dto;

public record UpdateItemRequest(
        String name,
        String description,
        Boolean available) {
}
