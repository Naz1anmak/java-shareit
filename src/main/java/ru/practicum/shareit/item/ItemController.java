package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

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
    public ItemDto getItemById(@PathVariable("itemId") long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getPersonalItems(@RequestHeader(USER_ID_HEADER) long ownerId) {
        return itemService.getPersonalItems(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam("text") String text) {
        return itemService.searchItems(text);
    }
}
