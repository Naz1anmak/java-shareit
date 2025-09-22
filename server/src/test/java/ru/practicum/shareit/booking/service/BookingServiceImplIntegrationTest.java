package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {
    @Autowired
    private BookingServiceImpl bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User("Owner", "owner@test.com"));
        booker = userRepository.save(new User("Booker", "booker@test.com"));
        item = itemRepository.save(new Item("Drill", "Powerful drill", true, owner));

        booking = bookingRepository.save(new Booking(
                item, booker,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                BookingStatus.WAITING
        ));
    }

    @Test
    void createBooking_shouldCreateBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        NewBookingRequest request = new NewBookingRequest(item.getId(), start, end);

        BookingDto bookingDto = bookingService.createBooking(request, booker.getId());

        assertThat(bookingDto).isNotNull();
        assertThat(bookingDto.item().id()).isEqualTo(item.getId());
        assertThat(bookingDto.booker().id()).isEqualTo(booker.getId());
        assertThat(bookingDto.status()).isEqualTo(BookingStatus.WAITING.toString());
    }

    @Test
    void createBooking_shouldThrowWhenItemNotAvailable() {
        Item unavailableItem = itemRepository.save(new Item("Unavailable", "Desc", false, owner));
        NewBookingRequest request = new NewBookingRequest(
                unavailableItem.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThatThrownBy(() -> bookingService.createBooking(request, booker.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Item is not available for booking");
    }

    @Test
    void createBooking_shouldThrowWhenOwnerBooksOwnItem() {
        NewBookingRequest request = new NewBookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThatThrownBy(() -> bookingService.createBooking(request, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Owner cannot book their own item");
    }

    @Test
    void createBooking_shouldThrowWhenStartAfterEnd() {
        NewBookingRequest request = new NewBookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookingService.createBooking(request, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Start date must be before end date");
    }

    @Test
    void approveBooking_shouldApproveBooking() {
        BookingDto result = bookingService.approveBooking(booking.getId(), true, owner.getId());

        assertThat(result.status()).isEqualTo(BookingStatus.APPROVED.toString());

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBooking_shouldRejectBooking() {
        BookingDto result = bookingService.approveBooking(booking.getId(), false, owner.getId());

        assertThat(result.status()).isEqualTo(BookingStatus.REJECTED.toString());

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveBooking_shouldThrowWhenNotOwner() {
        User otherUser = userRepository.save(new User("Other", "other@test.com"));

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), true, otherUser.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the owner can approve or reject the booking");
    }

    @Test
    void approveBooking_shouldThrowWhenStatusNotWaiting() {
        booking.approve();
        bookingRepository.save(booking);

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), true, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Booking has already been approved or rejected");
    }

    @Test
    void getBookingById_shouldReturnForBooker() {
        BookingDto result = bookingService.getBookingById(booking.getId(), booker.getId());

        assertThat(result.id()).isEqualTo(booking.getId());
        assertThat(result.item().id()).isEqualTo(item.getId());
        assertThat(result.booker().id()).isEqualTo(booker.getId());
    }

    @Test
    void getBookingById_shouldReturnForOwner() {
        BookingDto result = bookingService.getBookingById(booking.getId(), owner.getId());

        assertThat(result.id()).isEqualTo(booking.getId());
        assertThat(result.item().id()).isEqualTo(item.getId());
    }

    @Test
    void getBookingById_shouldThrowWhenUserNotRelated() {
        User stranger = userRepository.save(new User("Stranger", "stranger@test.com"));

        assertThatThrownBy(() -> bookingService.getBookingById(booking.getId(), stranger.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Access denied: User is neither the booker nor the item owner");
    }

    @Test
    void getBookingsByUser_shouldReturnBookingsForAllStates() {
        Booking pastBooking = bookingRepository.save(new Booking(
                item, booker,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(3),
                BookingStatus.APPROVED
        ));

        Booking currentBooking = bookingRepository.save(new Booking(
                item, booker,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                BookingStatus.APPROVED
        ));

        assertThat(bookingService.getBookingsByUser(BookingState.ALL, booker.getId())).hasSize(3);
        assertThat(bookingService.getBookingsByUser(BookingState.CURRENT, booker.getId())).hasSize(1);
        assertThat(bookingService.getBookingsByUser(BookingState.PAST, booker.getId())).hasSize(1);
        assertThat(bookingService.getBookingsByUser(BookingState.FUTURE, booker.getId())).hasSize(1);
        assertThat(bookingService.getBookingsByUser(BookingState.WAITING, booker.getId())).hasSize(1);
        assertThat(bookingService.getBookingsByUser(BookingState.REJECTED, booker.getId())).isEmpty();
    }

    @Test
    void getBookingsByOwner_shouldReturnBookingsForAllStates() {
        User anotherBooker = userRepository.save(new User("Another", "another@test.com"));

        Booking pastBooking = bookingRepository.save(new Booking(
                item, anotherBooker,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(3),
                BookingStatus.APPROVED
        ));

        Booking currentBooking = bookingRepository.save(new Booking(
                item, anotherBooker,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                BookingStatus.APPROVED
        ));

        assertThat(bookingService.getBookingsByOwner(BookingState.ALL, owner.getId())).hasSize(3);
        assertThat(bookingService.getBookingsByOwner(BookingState.CURRENT, owner.getId())).hasSize(1);
        assertThat(bookingService.getBookingsByOwner(BookingState.PAST, owner.getId())).hasSize(1);
        assertThat(bookingService.getBookingsByOwner(BookingState.FUTURE, owner.getId())).hasSize(1);
    }

    @Test
    void getBookingByIdOrThrow_shouldReturnBooking() {
        Booking result = bookingService.getBookingByIdOrThrow(booking.getId());

        assertThat(result.getId()).isEqualTo(booking.getId());
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getBooker()).isEqualTo(booker);
    }

    @Test
    void getBookingByIdOrThrow_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> bookingService.getBookingByIdOrThrow(999L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Booking not found with ID: 999");
    }

    @Test
    void getBookingsByUser_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> bookingService.getBookingsByUser(BookingState.ALL, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookingsByOwner_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> bookingService.getBookingsByOwner(BookingState.ALL, 999L))
                .isInstanceOf(NotFoundException.class);
    }
}
