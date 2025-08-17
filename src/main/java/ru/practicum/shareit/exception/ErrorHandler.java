package ru.practicum.shareit.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException exception, HttpServletRequest request) {
        return new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationException(ValidationException exception, HttpServletRequest request) {
        return new ErrorResponse(
                request.getRequestURI(),
                HttpStatus.CONFLICT.value(),
                exception.getMessage()
        );
    }
}
