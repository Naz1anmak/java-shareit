package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    @ToString.Exclude
    private User booker;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    public Booking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        this.item = item;
        this.booker = booker;
        this.start = start;
        this.end = end;
        this.status = status;
    }

    public void approve() {
        this.status = BookingStatus.APPROVED;
    }

    public void reject() {
        this.status = BookingStatus.REJECTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        return id != null && id.equals(((Booking) o).getId());
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
