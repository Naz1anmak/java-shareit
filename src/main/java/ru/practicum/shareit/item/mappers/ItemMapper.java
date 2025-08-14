package ru.practicum.shareit.item.mappers;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static Item mapToItem(NewItemRequest request, long ownerId) {
        Item item = new Item();
        item.setName(request.name());
        item.setDescription(request.description());
        item.setAvailable(request.available());
        item.setOwnerId(ownerId);
        return item;
    }

    public static ItemDto mapToItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public static void updateItemFields(Item item, UpdateItemRequest request) {
        if (request.hasName()) {
            item.setName(request.name());
        }
        if (request.hasDescription()) {
            item.setDescription(request.description());
        }
        if (request.hasAvailable()) {
            item.setAvailable(request.available());
        }
    }
}
