package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ItemWithCommentDto(
        Long id,
        String name,
        String description,
        Boolean available,
        LocalDateTime lastBooking,
        LocalDateTime nextBooking,
        List<CommentDto> comments) {
}
