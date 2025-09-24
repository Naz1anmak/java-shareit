package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemServiceImpl itemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private User anotherUser;
    private Item item;
    private ItemRequest itemRequest;
    private Booking pastBooking;
    private Booking futureBooking;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User("Owner", "owner@test.com"));
        booker = userRepository.save(new User("Booker", "booker@test.com"));
        anotherUser = userRepository.save(new User("Another", "another@test.com"));

        item = itemRepository.save(new Item("Drill", "Powerful drill", true, owner));

        itemRequest = itemRequestRepository.save(new ItemRequest("Need a drill", booker));

        LocalDateTime now = LocalDateTime.now();
        pastBooking = bookingRepository.save(new Booking(
                item, booker, now.minusDays(5), now.minusDays(3), BookingStatus.APPROVED
        ));

        futureBooking = bookingRepository.save(new Booking(
                item, booker, now.plusDays(1), now.plusDays(3), BookingStatus.WAITING
        ));
    }

    @Test
    void createItem_shouldCreateItem() {
        NewItemRequest request = new NewItemRequest("Hammer", "Heavy hammer", true, null);

        ItemDto result = itemService.createItem(request, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isPositive();
        assertThat(result.name()).isEqualTo("Hammer");
        assertThat(result.description()).isEqualTo("Heavy hammer");
        assertThat(result.available()).isTrue();
    }

    @Test
    void createItem_shouldCreateItemWithRequest() {
        NewItemRequest request = new NewItemRequest("Saw", "Sharp saw", true, itemRequest.getId());

        ItemDto result = itemService.createItem(request, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Saw");
    }

    @Test
    void createItem_shouldThrowWhenUserNotFound() {
        NewItemRequest request = new NewItemRequest("Test", "Desc", true, null);

        assertThatThrownBy(() -> itemService.createItem(request, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getItemById_shouldReturnWithBookingsForOwner() {
        ItemWithCommentDto result = itemService.getItemById(item.getId(), owner.getId());

        assertThat(result.id()).isEqualTo(item.getId());
        assertThat(result.name()).isEqualTo("Drill");
        assertThat(result.lastBooking()).isNotNull();
        assertThat(result.nextBooking()).isNotNull();
    }

    @Test
    void getItemById_shouldReturnWithoutBookingsForOtherUser() {
        ItemWithCommentDto result = itemService.getItemById(item.getId(), anotherUser.getId());

        assertThat(result.id()).isEqualTo(item.getId());
        assertThat(result.name()).isEqualTo("Drill");
        assertThat(result.lastBooking()).isNull();
        assertThat(result.nextBooking()).isNull();
    }

    @Test
    void getItemById_shouldThrowWhenItemNotFound() {
        assertThatThrownBy(() -> itemService.getItemById(999L, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Item not found with ID: 999");
    }

    @Test
    void getPersonalItems_shouldReturnEmptyListWhenNoItems() {
        User userWithoutItems = userRepository.save(new User("NoItems", "noitems@test.com"));

        List<ItemWithCommentDto> result = itemService.getPersonalItems(userWithoutItems.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void updateItem_shouldUpdateItem() {
        UpdateItemRequest request = new UpdateItemRequest("Updated Drill", "Updated description", false);

        ItemDto result = itemService.updateItem(item.getId(), request, owner.getId());

        assertThat(result.name()).isEqualTo("Updated Drill");
        assertThat(result.description()).isEqualTo("Updated description");
        assertThat(result.available()).isFalse();
    }

    @Test
    void updateItem_shouldUpdatePartialFields() {
        UpdateItemRequest request = new UpdateItemRequest("New Name", null, null);

        ItemDto result = itemService.updateItem(item.getId(), request, owner.getId());

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.description()).isEqualTo("Powerful drill");
        assertThat(result.available()).isTrue();
    }

    @Test
    void updateItem_shouldThrowWhenNotOwner() {
        UpdateItemRequest request = new UpdateItemRequest("New Name", "Desc", true);

        assertThatThrownBy(() -> itemService.updateItem(item.getId(), request, anotherUser.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Item with ID " + item.getId() + " does not belong to you");
    }

    @Test
    void updateItem_shouldThrowWhenItemNotFound() {
        UpdateItemRequest request = new UpdateItemRequest("Name", "Desc", true);

        assertThatThrownBy(() -> itemService.updateItem(999L, request, owner.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void searchItems_shouldReturnMatchingItems() {
        List<ItemDto> result = itemService.searchItems("drill");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Drill");
    }

    @Test
    void searchItems_shouldReturnEmptyListWhenNoMatches() {
        List<ItemDto> result = itemService.searchItems("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void searchItems_shouldReturnEmptyListWhenBlankText() {
        List<ItemDto> result = itemService.searchItems("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void searchItems_shouldNotReturnUnavailableItems() {
        Item unavailableItem = itemRepository.save(new Item("Broken Drill", "Not working", false, owner));

        List<ItemDto> result = itemService.searchItems("drill");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Drill");
    }

    @Test
    void getItemByIdOrThrow_shouldReturnItem() {
        Item result = itemService.getItemByIdOrThrow(item.getId());

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo("Drill");
    }

    @Test
    void getItemByIdOrThrow_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> itemService.getItemByIdOrThrow(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Item not found with ID: 999");
    }

    @Test
    void addComment_shouldAddComment() {
        NewCommentRequest commentRequest = new NewCommentRequest("Great item!");

        CommentDto result = itemService.addComment(item.getId(), booker.getId(), commentRequest);

        assertThat(result).isNotNull();
        assertThat(result.text()).isEqualTo("Great item!");
        assertThat(result.authorName()).isEqualTo("Booker");

        List<Comment> comments = commentRepository.findByItemId(item.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().getText()).isEqualTo("Great item!");
    }

    @Test
    void addComment_shouldThrowWhenOwnerTriesToComment() {
        NewCommentRequest commentRequest = new NewCommentRequest("My own item");

        assertThatThrownBy(() -> itemService.addComment(item.getId(), owner.getId(), commentRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("You cannot comment on your own item");
    }

    @Test
    void addComment_shouldThrowWhenUserNeverBooked() {
        NewCommentRequest commentRequest = new NewCommentRequest("Comment");

        assertThatThrownBy(() -> itemService.addComment(item.getId(), anotherUser.getId(), commentRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You can only comment on items you have previously booked and had booking approved");
    }

    @Test
    void addComment_shouldThrowWhenOnlyFutureBookingExists() {
        User futureBooker = userRepository.save(new User("Future", "future@test.com"));
        bookingRepository.save(new Booking(
                item, futureBooker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3), BookingStatus.WAITING
        ));

        NewCommentRequest commentRequest = new NewCommentRequest("Comment");

        assertThatThrownBy(() -> itemService.addComment(item.getId(), futureBooker.getId(), commentRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void addComment_shouldThrowWhenItemNotFound() {
        NewCommentRequest commentRequest = new NewCommentRequest("Comment");

        assertThatThrownBy(() -> itemService.addComment(999L, booker.getId(), commentRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addComment_shouldThrowWhenUserNotFound() {
        NewCommentRequest commentRequest = new NewCommentRequest("Comment");

        assertThatThrownBy(() -> itemService.addComment(item.getId(), 999L, commentRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPersonalItems_shouldIncludeComments() {
        pastBooking = bookingRepository.save(new Booking(
                item, booker, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(1), BookingStatus.APPROVED
        ));

        NewCommentRequest commentRequest = new NewCommentRequest("Good item!");
        itemService.addComment(item.getId(), booker.getId(), commentRequest);

        entityManager.flush();
        entityManager.clear();

        List<ItemWithCommentDto> result = itemService.getPersonalItems(owner.getId());

        assertThat(result).hasSize(1);

        ItemWithCommentDto itemDto = result.getFirst();
        assertThat(itemDto.comments()).hasSize(1);
        assertThat(itemDto.comments().getFirst().text()).isEqualTo("Good item!");
    }
}