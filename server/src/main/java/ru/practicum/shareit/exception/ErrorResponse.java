package ru.practicum.shareit.exception;

public record ErrorResponse(
        String path,
        int statusCode,
        String error,
        String message) {
}
