package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

public interface ItemService {
    ItemDto createItem(NewItemRequest request, long userId);

    ItemDto getItemById(long itemId);

    List<ItemDto> getPersonalItems(long userId);

    ItemDto updateItem(long itemId, UpdateItemRequest request, long userId);

    List<ItemDto> searchItems(String text);
}
