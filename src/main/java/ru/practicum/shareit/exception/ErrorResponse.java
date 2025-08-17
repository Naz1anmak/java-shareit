package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final String path;
    private final int status;
    private final String message;
}
