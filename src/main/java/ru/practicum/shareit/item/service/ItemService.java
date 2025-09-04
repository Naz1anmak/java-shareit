package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(NewItemRequest request, long userId);

    ItemWithCommentDto getItemById(long itemId, long userId);

    List<ItemWithCommentDto> getPersonalItems(long userId);

    ItemDto updateItem(long itemId, UpdateItemRequest request, long userId);

    List<ItemDto> searchItems(String text);

    Item getItemByIdOrThrow(long itemId);

    CommentDto addComment(long itemId, long userId, String text);
}
