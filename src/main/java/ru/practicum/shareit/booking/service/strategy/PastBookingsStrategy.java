package ru.practicum.shareit.booking.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PastBookingsStrategy implements BookingFetchStrategy {
    private final BookingRepository bookingRepository;

    @Override
    public BookingState getState() {
        return BookingState.PAST;
    }

    @Override
    public List<Booking> fetchBookings(long userId, boolean isOwner, LocalDateTime now) {
        return isOwner
                ? bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now)
                : bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
    }
}
