package ru.practicum.shareit.item.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "request.name", target = "name")
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Item fromNewRequest(NewItemRequest request, User owner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateItemFromRequest(@MappingTarget Item item, UpdateItemRequest request);

    CommentDto toCommentDto(Comment comment);

    ItemWithCommentDto toItemWithCommentDto(Item item, LocalDateTime lastBooking, LocalDateTime nextBooking);
}
