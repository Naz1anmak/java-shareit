package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    @Test
    void testNoArgsConstructor() {
        Booking booking = new Booking();
        assertNotNull(booking);
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User("John", "john@test.com");
        user.setId(1L);

        Item item = new Item("Drill", "Powerful drill", true, user);
        item.setId(1L);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(3);

        Booking booking = new Booking(item, user, start, end, BookingStatus.WAITING);

        assertNotNull(booking);
        assertEquals(item, booking.getItem());
        assertEquals(user, booking.getBooker());
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        Booking booking = new Booking();

        booking.setId(1L);
        assertEquals(1L, booking.getId());

        User user = new User("John", "john@test.com");
        booking.setBooker(user);
        assertEquals(user, booking.getBooker());

        Item item = new Item("Drill", "Desc", true, user);
        booking.setItem(item);
        assertEquals(item, booking.getItem());

        LocalDateTime start = LocalDateTime.now();
        booking.setStart(start);
        assertEquals(start, booking.getStart());

        LocalDateTime end = start.plusDays(1);
        booking.setEnd(end);
        assertEquals(end, booking.getEnd());

        booking.setStatus(BookingStatus.APPROVED);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void testApprove() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.WAITING);

        booking.approve();
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void testReject() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.WAITING);

        booking.reject();
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        Booking booking1 = new Booking();
        booking1.setId(1L);

        Booking booking2 = new Booking();
        booking2.setId(1L);

        Booking booking3 = new Booking();
        booking3.setId(2L);

        assertEquals(booking1, booking2);
        assertEquals(booking2, booking1);

        assertEquals(booking1.hashCode(), booking2.hashCode());

        assertNotEquals(booking1, booking3);

        assertNotEquals(null, booking1);
    }

    @Test
    void testToString() {
        Booking booking = new Booking();
        booking.setId(1L);

        String toString = booking.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Booking"));
        assertFalse(toString.contains("item="));
        assertFalse(toString.contains("booker="));
    }
}