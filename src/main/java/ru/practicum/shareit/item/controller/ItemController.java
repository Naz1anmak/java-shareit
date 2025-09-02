package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.constants.HeaderConstants.USER_ID_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody NewItemRequest request,
                              @RequestHeader(USER_ID_HEADER) long ownerId) {
        return itemService.createItem(request, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable("itemId") long itemId,
                              @RequestBody UpdateItemRequest request,
                              @RequestHeader(USER_ID_HEADER) long ownerId) {
        return itemService.updateItem(itemId, request, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemWithCommentDto getItemById(@PathVariable("itemId") long itemId,
                                          @RequestHeader(USER_ID_HEADER) long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemWithCommentDto> getPersonalItems(@RequestHeader(USER_ID_HEADER) long ownerId) {
        return itemService.getPersonalItems(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam("text") String text) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable("itemId") long itemId,
                                 @RequestHeader(USER_ID_HEADER) long userId,
                                 @RequestBody @Valid NewCommentRequest body) {
        return itemService.addComment(itemId, userId, body.text());
    }
}
