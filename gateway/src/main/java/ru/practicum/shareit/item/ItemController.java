package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import static ru.practicum.shareit.constants.HeaderConstants.USER_ID_HEADER;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_HEADER) long ownerId,
                                             @RequestBody @Valid NewItemRequest request) {
        log.info("Gateway: createItem ownerId={}, request={}", ownerId, request);
        return itemClient.createItem(ownerId, request);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_HEADER) long ownerId,
                                             @Positive @PathVariable("itemId") long itemId,
                                             @RequestBody UpdateItemRequest request) {
        log.info("Gateway: updateItem itemId={}, ownerId={}, request={}", itemId, ownerId, request);
        return itemClient.updateItem(itemId, ownerId, request);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID_HEADER) long userId,
                                              @Positive @PathVariable("itemId") long itemId) {
        log.info("Gateway: getItemById itemId={}, userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getPersonalItems(@RequestHeader(USER_ID_HEADER) long ownerId) {
        log.info("Gateway: getPersonalItems ownerId={}", ownerId);
        return itemClient.getPersonalItems(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@NotBlank @RequestParam("text") String text) {
        log.info("Gateway: searchItems text={}", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) long userId,
                                             @Positive @PathVariable("itemId") long itemId,
                                             @RequestBody @Valid NewCommentRequest comment) {
        log.info("Gateway: addComment userId={}, itemId={}, comment={}", userId, itemId, comment);
        return itemClient.addComment(userId, itemId, comment);
    }
}
