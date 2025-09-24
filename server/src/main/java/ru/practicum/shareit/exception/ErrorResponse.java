package ru.practicum.shareit.exception;

public record ErrorResponse(String path, int status, String message) {
}
