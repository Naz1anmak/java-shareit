package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.constants.HeaderConstants.USER_ID_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@Valid @RequestBody NewBookingRequest request,
                                    @RequestHeader(USER_ID_HEADER) long bookerId) {
        return bookingService.createBooking(request, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable("bookingId") long bookingId,
                                     @RequestParam("approved") boolean approved,
                                     @RequestHeader(USER_ID_HEADER) long ownerId) {
        return bookingService.approveBooking(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable("bookingId") long bookingId,
                                     @RequestHeader(USER_ID_HEADER) long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByUser(@RequestParam(value = "state", defaultValue = "ALL") BookingState state,
                                              @RequestHeader(USER_ID_HEADER) long userId) {
        return bookingService.getBookingsByUser(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestParam(value = "state", defaultValue = "ALL") BookingState state,
                                               @RequestHeader(USER_ID_HEADER) long ownerId) {
        return bookingService.getBookingsByOwner(state, ownerId);
    }
}
