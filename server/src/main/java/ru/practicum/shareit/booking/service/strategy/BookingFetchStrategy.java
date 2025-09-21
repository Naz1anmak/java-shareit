package ru.practicum.shareit.booking.service.strategy;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingFetchStrategy {
    BookingState getState();

    List<Booking> fetchBookings(long userId, boolean isOwner, LocalDateTime now);
}
