package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(@Valid NewBookingRequest request, long bookerId);

    BookingDto approveBooking(long bookingId, boolean approved, long ownerId);

    BookingDto getBookingById(long bookingId, long userId);

    List<BookingDto> getBookingsByUser(BookingState state, long userId);

    List<BookingDto> getBookingsByOwner(BookingState state, long ownerId);

    Booking getBookingByIdOrThrow(long bookingId);
}
