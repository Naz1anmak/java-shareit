package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(NewItemRequest request, long ownerId) {
        User owner = userService.getUserByIdOrThrow(ownerId);
        Item item = itemMapper.fromNewRequest(request, owner);
        item = itemRepository.save(item);
        log.info("Добавлена новая вещь \"{}\" c id {}", item.getName(), item.getId());
        return itemMapper.toDto(item);
    }

    @Override
    public ItemWithCommentDto getItemById(long itemId, long userId) {
        Item item = itemRepository.findByIdWithComments(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with ID: " + itemId));

        if (item.getOwner().getId() == userId) {
            return getLastAndNextBooking(List.of(item)).getFirst();
        } else {
            return itemMapper.toItemWithCommentDto(item, null, null);
        }
    }

    @Override
    public List<ItemWithCommentDto> getPersonalItems(long ownerId) {
        userService.getUserById(ownerId);

        List<Item> items = itemRepository.findByOwnerIdWithComments(ownerId);
        return getLastAndNextBooking(items);
    }

    @Override
    @Transactional
    public ItemDto updateItem(long itemId, UpdateItemRequest request, long ownerId) {
        userService.getUserById(ownerId);
        Item item = getItemByIdOrThrow(itemId);
        if (item.getOwner().getId() != ownerId) {
            throw new ValidationException("Item with ID " + itemId + " does not belong to you");
        }
        itemMapper.updateItemFromRequest(item, request);
        item = itemRepository.save(item);
        log.info("Обновлена вещь \"{}\" с id {}", item.getName(), item.getId());
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) return List.of();
        return itemRepository.searchByNameOrDescriptionAndAvailableIsTrue(text).stream()
                .map(itemMapper::toDto)
                .toList();
    }

    @Override
    public Item getItemByIdOrThrow(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with ID: " + itemId));
    }

    @Override
    @Transactional
    public CommentDto addComment(long itemId, long userId, String text) {
        User user = userService.getUserByIdOrThrow(userId);
        Item item = getItemByIdOrThrow(itemId);
        if (item.getOwner().getId() == userId) {
            throw new ValidationException("You cannot comment on your own item");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean hasPastBooking = bookingRepository
                .existsByItemIdAndBookerIdAndEndBeforeAndStatus(itemId, userId, now, BookingStatus.APPROVED);
        if (!hasPastBooking) {
            throw new BadRequestException("You can only comment on items you have previously booked and had booking approved");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setAuthorName(user.getName());
        comment.setCreated(now);
        comment.setItem(item);
        comment.setAuthor(user);

        comment = commentRepository.save(comment);
        log.info("Добавлен комментарий к вещи с id {} от пользователя с id {}", itemId, userId);
        return itemMapper.toCommentDto(comment);
    }

    private List<ItemWithCommentDto> getLastAndNextBooking(List<Item> items) {
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, List<BookingForItemDto>> bookingsByItem = bookingRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(BookingForItemDto::itemId));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    List<BookingForItemDto> bookings = bookingsByItem.getOrDefault(item.getId(), List.of());

                    BookingForItemDto last = bookings.stream()
                            .filter(b -> b.end().isBefore(now))
                            .max(Comparator.comparing(BookingForItemDto::end))
                            .orElse(null);

                    BookingForItemDto next = bookings.stream()
                            .filter(b -> b.start().isAfter(now))
                            .min(Comparator.comparing(BookingForItemDto::start))
                            .orElse(null);

                    LocalDateTime lastStart = last != null ? last.start() : null;
                    LocalDateTime nextStart = next != null ? next.start() : null;

                    return itemMapper.toItemWithCommentDto(item, lastStart, nextStart);
                })
                .toList();
    }
}
