package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import static ru.practicum.shareit.constants.HeaderConstants.USER_ID_HEADER;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID_HEADER) long userId,
                                                @RequestBody @Valid NewItemRequestDto request) {
        log.info("Gateway: createRequest userId={}, request={}", userId, request);
        return itemRequestClient.createRequest(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsByOwner(@RequestHeader(USER_ID_HEADER) long ownerId) {
        log.info("Gateway: getRequestByOwner ownerId={}", ownerId);
        return itemRequestClient.getRequestsByOwner(ownerId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests() {
        log.info("Gateway: getAllRequests");
        return itemRequestClient.getRequests();
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@Positive @PathVariable("requestId") long requestId) {
        log.info("Gateway: getRequestById requestId={}", requestId);
        return itemRequestClient.getRequestById(requestId);
    }
}
