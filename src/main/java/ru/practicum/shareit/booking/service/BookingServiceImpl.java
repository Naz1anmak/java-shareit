package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.strategy.BookingFetchStrategy;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final Map<BookingState, BookingFetchStrategy> strategies;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ItemService itemService;
    private final UserService userService;

    public BookingServiceImpl(List<BookingFetchStrategy> strategies,
                              BookingRepository bookingRepository,
                              BookingMapper bookingMapper,
                              ItemService itemService,
                              UserService userService) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(BookingFetchStrategy::getState, strategy -> strategy));
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.itemService = itemService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public BookingDto createBooking(NewBookingRequest request, long bookerId) {
        User booker = userService.getUserByIdOrThrow(bookerId);
        Item item = itemService.getItemByIdOrThrow(request.itemId());
        if (!item.getAvailable()) {
            throw new BadRequestException("Item is not available for booking");
        }

        if (Objects.equals(item.getOwner().getId(), booker.getId())) {
            throw new ValidationException("Owner cannot book their own item");
        }

        if (request.start().isAfter(request.end())) {
            throw new ValidationException("Start date must be before end date");
        }

        Booking booking = bookingMapper.fromNewRequest(request, item, booker, BookingStatus.WAITING);
        booking = bookingRepository.save(booking);
        log.info("Отправлен запрос на бронирование с id {} вещи с id {} пользователем с id {}",
                booking.getId(),
                item.getId(),
                booker.getId());
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(long bookingId, boolean approved, long ownerId) {
        Booking booking = getBookingByIdOrThrow(bookingId);
        if (booking.getItem().getOwner().getId() != ownerId) {
            throw new ForbiddenException("Only the owner can approve or reject the booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking has already been approved or rejected");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        booking = bookingRepository.save(booking);
        log.info("Бронирование с id {} вещи с id {} обновлено. Статус: {}",
                booking.getId(),
                booking.getItem().getId(),
                booking.getStatus());
        return bookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBookingById(long bookingId, long userId) {
        Booking booking = getBookingByIdOrThrow(bookingId);
        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new ValidationException("Access denied: User is neither the booker nor the item owner");
        }
        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByUser(BookingState state, long bookerId) {
        userService.getUserByIdOrThrow(bookerId);

        LocalDateTime now = LocalDateTime.now();
        return strategies.get(state).fetchBookings(bookerId, false, now).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getBookingsByOwner(BookingState state, long ownerId) {
        userService.getUserByIdOrThrow(ownerId);

        LocalDateTime now = LocalDateTime.now();
        return strategies.get(state).fetchBookings(ownerId, true, now).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public Booking getBookingByIdOrThrow(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ValidationException("Booking not found with ID: " + bookingId));
    }
}
