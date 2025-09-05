package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithInfoDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.constants.HeaderConstants.USER_ID_HEADER;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader(USER_ID_HEADER) long userId,
                                        @RequestBody @Valid NewItemRequestDto requestDto) {
        return itemRequestService.createRequest(userId, requestDto.description());
    }

    @GetMapping
    public List<ItemRequestWithInfoDto> getRequestsByOwner(@RequestHeader(USER_ID_HEADER) long userId) {
        return itemRequestService.getRequestsByOwner(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests() {
        return itemRequestService.getAllRequests();
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithInfoDto getRequestById(@PathVariable long requestId) {
        return itemRequestService.getRequestById(requestId);
    }
}
