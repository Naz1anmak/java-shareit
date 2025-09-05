package ru.practicum.shareit.request.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithInfoDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.QItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemRepository itemRepository;


    @Override
    @Transactional
    public ItemRequestDto createRequest(long userId, String description) {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Description cannot be null or blank");
        }
        checkDescriptionExists(description);
        User requester = userService.getUserByIdOrThrow(userId);

        ItemRequest itemRequest = itemRequestRepository.save(new ItemRequest(description, requester));
        log.info("Создан новый запрос вещи с id {} от пользователя с id {}", itemRequest.getId(), userId);
        return itemRequestMapper.toDto(itemRequest);
    }

    public List<ItemRequestWithInfoDto> getRequestsByOwner(long userId) {
        userService.getUserByIdOrThrow(userId);

        QItemRequest itemRequest = QItemRequest.itemRequest;
        BooleanExpression byOwner = itemRequest.requester.id.eq(userId);

        List<ItemRequest> requests = (List<ItemRequest>) itemRequestRepository.findAll(byOwner,
                Sort.by(Sort.Direction.DESC, "created"));

        return mapToDtoWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        return itemRequestRepository.findAllByOrderByCreatedDesc().stream()
                .map(itemRequestMapper::toDto)
                .toList();
    }

    @Override
    public ItemRequestWithInfoDto getRequestById(long requestId) {
        QItemRequest itemRequest = QItemRequest.itemRequest;
        BooleanExpression byId = itemRequest.id.eq(requestId);

        List<ItemRequest> requests = (List<ItemRequest>) itemRequestRepository.findAll(byId);

        if (requests.isEmpty()) {
            throw new NotFoundException("Request with id=" + requestId + " not found");
        }

        return mapToDtoWithItems(requests).getFirst();
    }

    @Override
    public ItemRequest getItemRequestByIdOrThrow(long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found with ID: " + requestId));
    }

    private void checkDescriptionExists(String description) {
        if (itemRequestRepository.existsByDescription(description)) {
            throw new ValidationException("Request with this description already exists");
        }
    }

    private List<ItemRequestWithInfoDto> mapToDtoWithItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemInfoDto>> itemsByRequestId = itemRepository
                .findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(item -> new ItemInfoDto(
                                item.getId(),
                                item.getName(),
                                item.getOwner().getId()
                        ), Collectors.toList())
                ));

        return requests.stream()
                .map(req -> new ItemRequestWithInfoDto(
                        req.getId(),
                        req.getDescription(),
                        req.getCreated(),
                        itemsByRequestId.getOrDefault(req.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }
}
