package ru.practicum.shareit.booking.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryIntegrationTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;
    private LocalDateTime now;
    private Booking pastBooking;
    private Booking currentBooking;
    private Booking futureBooking;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("Owner", "owner@test.com"));
        booker = userRepository.save(new User("Booker", "booker@test.com"));

        item = itemRepository.save(new Item("TestName", "testDescription", true, owner));

        now = LocalDateTime.now();

        pastBooking = bookingRepository.save(new Booking(
                item, booker, now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED));

        currentBooking = bookingRepository.save(new Booking(
                item, booker, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED));

        futureBooking = bookingRepository.save(new Booking(
                item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING));
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findByBookerIdOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByBookerIdOrderByStartDesc(booker.getId());

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Booking::getId)
                .containsExactly(futureBooking.getId(), currentBooking.getId(), pastBooking.getId());
    }

    @Test
    void findByItemOwnerIdOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByItemOwnerIdOrderByStartDesc(owner.getId());

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Booking::getId)
                .containsExactly(futureBooking.getId(), currentBooking.getId(), pastBooking.getId());
    }

    @Test
    void findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                booker.getId(), now, now);

        assertThat(result).containsExactly(currentBooking);
    }

    @Test
    void findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                owner.getId(), now, now);

        assertThat(result).containsExactly(currentBooking);
    }

    @Test
    void findByBookerIdAndEndBeforeOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(booker.getId(), now);

        assertThat(result).containsExactly(pastBooking);
    }

    @Test
    void findByItemOwnerIdAndEndBeforeOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(owner.getId(), now);

        assertThat(result).containsExactly(pastBooking);
    }

    @Test
    void findByBookerIdAndStartAfterOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(booker.getId(), now);

        assertThat(result).containsExactly(futureBooking);
    }

    @Test
    void findByItemOwnerIdAndStartAfterOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(owner.getId(), now);

        assertThat(result).containsExactly(futureBooking);
    }

    @Test
    void findByBookerIdAndStatusOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(booker.getId(), BookingStatus.WAITING);

        assertThat(result).containsExactly(futureBooking);
    }

    @Test
    void findByItemOwnerIdAndStatusOrderByStartDesc() {
        List<Booking> result = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingStatus.APPROVED);

        assertThat(result).containsExactly(currentBooking, pastBooking);
    }

    @Test
    void findByItemIdIn() {
        List<BookingForItemDto> result = bookingRepository.findByItemIdIn(List.of(item.getId()));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(BookingForItemDto::itemId)
                .containsOnly(item.getId());
    }

    @Test
    void existsByItemIdAndBookerIdAndEndBeforeAndStatus() {
        boolean exists = bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                item.getId(), booker.getId(), now, BookingStatus.APPROVED);

        assertThat(exists).isTrue();

        boolean notExists = bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                item.getId(), booker.getId(), now, BookingStatus.REJECTED);

        assertThat(notExists).isFalse();
    }
}
