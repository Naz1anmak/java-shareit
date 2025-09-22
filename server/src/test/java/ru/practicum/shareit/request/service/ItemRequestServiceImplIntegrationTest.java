package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestServiceImpl itemRequestService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User requester;
    private User anotherUser;
    private User itemOwner;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        requester = userRepository.save(new User("Requester", "requester@test.com"));
        anotherUser = userRepository.save(new User("Another", "another@test.com"));
        itemOwner = userRepository.save(new User("Owner", "owner@test.com"));

        LocalDateTime now = LocalDateTime.now();
        itemRequest1 = itemRequestRepository.save(new ItemRequest("Need a drill", requester));
        itemRequest1.setCreated(now.minusDays(1));
        itemRequestRepository.save(itemRequest1);

        itemRequest2 = itemRequestRepository.save(new ItemRequest("Need a hammer", requester));
        itemRequest2.setCreated(now.minusDays(2));
        itemRequestRepository.save(itemRequest2);

        Item item1 = new Item("Drill", "Powerful drill", true, itemOwner);
        item1.setRequest(itemRequest1);
        itemRepository.save(item1);

        Item item2 = new Item("Hammer", "Heavy hammer", true, itemOwner);
        item2.setRequest(itemRequest1);
        itemRepository.save(item2);
    }

    @Test
    void createRequest_shouldCreateRequest() {
        String description = "Need a new laptop";

        ItemRequestDto result = itemRequestService.createRequest(requester.getId(), description);

        assertThat(result).isNotNull();
        assertThat(result.id()).isPositive();
        assertThat(result.description()).isEqualTo(description);
        assertThat(result.created()).isNotNull();

        List<ItemRequest> allRequests = itemRequestRepository.findAll();
        assertThat(allRequests).hasSize(3);
        assertThat(allRequests).extracting(ItemRequest::getDescription)
                .contains("Need a new laptop");
    }

    @Test
    void createRequest_shouldThrowWhenDescriptionIsNull() {
        assertThatThrownBy(() -> itemRequestService.createRequest(requester.getId(), null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Description cannot be null or blank");
    }

    @Test
    void createRequest_shouldThrowWhenDescriptionIsBlank() {
        assertThatThrownBy(() -> itemRequestService.createRequest(requester.getId(), "   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Description cannot be null or blank");
    }

    @Test
    void createRequest_shouldThrowWhenDescriptionAlreadyExists() {
        assertThatThrownBy(() -> itemRequestService.createRequest(anotherUser.getId(), "Need a drill"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Request with this description already exists");
    }

    @Test
    void createRequest_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.createRequest(999L, "New request"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getRequestsByOwner_shouldReturnRequestsInDescOrder() {
        List<ItemRequestWithInfoDto> result = itemRequestService.getRequestsByOwner(requester.getId());

        assertThat(result).hasSize(2);

        assertThat(result).extracting(ItemRequestWithInfoDto::id)
                .containsExactly(itemRequest1.getId(), itemRequest2.getId());

        assertThat(result.get(0).items()).hasSize(2);
        assertThat(result.get(0).items()).extracting(ItemInfoDto::name)
                .containsExactlyInAnyOrder("Drill", "Hammer");

        assertThat(result.get(1).items()).isEmpty();
    }

    @Test
    void getRequestsByOwner_shouldReturnEmptyListWhenNoRequests() {
        List<ItemRequestWithInfoDto> result = itemRequestService.getRequestsByOwner(anotherUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getRequestsByOwner_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getRequestsByOwner(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllRequests_shouldReturnAllRequestsInDescOrder() {
        ItemRequest anotherRequest = itemRequestRepository.save(new ItemRequest("Need a saw", anotherUser));

        List<ItemRequestDto> result = itemRequestService.getAllRequests();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(ItemRequestDto::id)
                .containsExactly(anotherRequest.getId(), itemRequest1.getId(), itemRequest2.getId());
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        ItemRequestWithInfoDto result = itemRequestService.getRequestById(itemRequest1.getId());

        assertThat(result.id()).isEqualTo(itemRequest1.getId());
        assertThat(result.description()).isEqualTo("Need a drill");
        assertThat(result.items()).hasSize(2);
        assertThat(result.items()).extracting(ItemInfoDto::name)
                .containsExactlyInAnyOrder("Drill", "Hammer");
    }

    @Test
    void getRequestById_shouldReturnRequestWithoutItems() {
        ItemRequestWithInfoDto result = itemRequestService.getRequestById(itemRequest2.getId());

        assertThat(result.id()).isEqualTo(itemRequest2.getId());
        assertThat(result.description()).isEqualTo("Need a hammer");
        assertThat(result.items()).isEmpty();
    }

    @Test
    void getRequestById_shouldThrowWhenRequestNotFound() {
        assertThatThrownBy(() -> itemRequestService.getRequestById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Request with id=999 not found");
    }

    @Test
    void getItemRequestByIdOrThrow_shouldReturnRequest() {
        ItemRequest result = itemRequestService.getItemRequestByIdOrThrow(itemRequest1.getId());

        assertThat(result.getId()).isEqualTo(itemRequest1.getId());
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getRequester()).isEqualTo(requester);
    }

    @Test
    void getItemRequestByIdOrThrow_shouldThrowWhenRequestNotFound() {
        assertThatThrownBy(() -> itemRequestService.getItemRequestByIdOrThrow(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Request not found with ID: 999");
    }

    @Test
    void getRequestsByOwner_shouldNotIncludeOtherUsersRequests() {
        ItemRequest anotherUserRequest = itemRequestRepository.save(new ItemRequest("Other request", anotherUser));

        List<ItemRequestWithInfoDto> result = itemRequestService.getRequestsByOwner(requester.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemRequestWithInfoDto::id)
                .doesNotContain(anotherUserRequest.getId());
    }

    @Test
    void mapToDtoWithItems_shouldGroupItemsByRequest() {
        Item item3 = new Item("Saw", "Sharp saw", true, itemOwner);
        item3.setRequest(itemRequest2);
        itemRepository.save(item3);

        List<ItemRequest> requests = Arrays.asList(itemRequest1, itemRequest2);

        List<ItemRequestWithInfoDto> result = itemRequestService.getRequestsByOwner(requester.getId());

        assertThat(result).hasSize(2);

        ItemRequestWithInfoDto request1Dto = result.stream()
                .filter(dto -> dto.id().equals(itemRequest1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(request1Dto.items()).hasSize(2);

        ItemRequestWithInfoDto request2Dto = result.stream()
                .filter(dto -> dto.id().equals(itemRequest2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(request2Dto.items()).hasSize(1);
        assertThat(request2Dto.items().getFirst().name()).isEqualTo("Saw");
    }
}