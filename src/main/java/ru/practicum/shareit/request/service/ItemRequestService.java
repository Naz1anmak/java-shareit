package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(long userId, String description);

    List<ItemRequestWithInfoDto> getRequestsByOwner(long userId);

    List<ItemRequestDto> getAllRequests();

    ItemRequestWithInfoDto getRequestById(long requestId);

    ItemRequest getItemRequestByIdOrThrow(long requestId);
}
