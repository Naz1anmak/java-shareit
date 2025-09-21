package ru.practicum.shareit.booking.controller;

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
    public BookingDto createBooking(@RequestHeader(USER_ID_HEADER) long bookerId,
                                    @RequestBody NewBookingRequest request) {
        return bookingService.createBooking(request, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader(USER_ID_HEADER) long ownerId,
                                     @PathVariable("bookingId") long bookingId,
                                     @RequestParam("approved") boolean approved) {
        return bookingService.approveBooking(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(USER_ID_HEADER) long userId,
                                     @PathVariable("bookingId") long bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByUser(@RequestHeader(USER_ID_HEADER) long userId,
                                              @RequestParam(value = "state") BookingState state) {
        return bookingService.getBookingsByUser(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestHeader(USER_ID_HEADER) long ownerId,
                                               @RequestParam(value = "state") BookingState state) {
        return bookingService.getBookingsByOwner(state, ownerId);
    }
}
