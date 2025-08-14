package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static ru.practicum.shareit.item.mappers.ItemMapper.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public ItemDto createItem(NewItemRequest request, long ownerId) {
        userService.getUserById(ownerId);
        Item item = mapToItem(request, ownerId);
        item.setId(getNextId());
        item = itemRepository.create(item);
        log.info("Добавлена новая вещь \"{}\" c id {}", item.getName(), item.getId());
        return mapToItemDto(item);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return mapToItemDto(getItemByIdOrThrow(itemId));
    }

    @Override
    public List<ItemDto> getPersonalItems(long ownerId) {
        userService.getUserById(ownerId);
        return itemRepository.getItems(ownerId)
                .stream()
                .map(ItemMapper::mapToItemDto)
                .toList();
    }

    @Override
    public ItemDto updateItem(long itemId, UpdateItemRequest request, long ownerId) {
        userService.getUserById(ownerId);
        Item item = getItemByIdOrThrow(itemId);
        if (item.getOwnerId() != ownerId) {
            throw new ValidationException("Item with ID " + itemId + " does not belong to you");
        }
        updateItemFields(item, request);
        item = itemRepository.update(item);
        log.info("Обновлена вещь \"{}\" с id {}", item.getName(), item.getId());
        return mapToItemDto(item);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) return List.of();
        return itemRepository.getItems().stream()
                .filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                        && item.getAvailable())
                .map(ItemMapper::mapToItemDto)
                .toList();
    }

    private Item getItemByIdOrThrow(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with ID: " + itemId));
    }

    private long getNextId() {
        return itemRepository.getItems().stream()
                .mapToLong(Item::getId)
                .max()
                .orElse(0) + 1;
    }
}
