package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemInfoDto;

import java.time.LocalDateTime;
import java.util.List;

public record ItemRequestWithInfoDto(
        Long id,
        String description,
        LocalDateTime created,
        List<ItemInfoDto> items
) {
}
