package com.example.Summit_Project.booking.service;

import com.example.Summit_Project.auth.entity.AppUser;
import com.example.Summit_Project.auth.repository.AppUserRepository;
import com.example.Summit_Project.booking.dto.AdminBookingView;
import com.example.Summit_Project.booking.dto.BookingRequest;
import com.example.Summit_Project.booking.dto.BookingResponse;
import com.example.Summit_Project.booking.dto.UserBookingHistoryResponse;
import com.example.Summit_Project.booking.entity.Booking;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.exception.BookingFailedException;
import com.example.Summit_Project.booking.exception.BookingNotFoundException;
import com.example.Summit_Project.booking.repository.BookingRepository;
import com.example.Summit_Project.booking.repository.RoomRepository;
import com.example.Summit_Project.booking.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private static final String ACTIVE = "ACTIVE";

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final AppUserRepository appUserRepository;

    public BookingService(
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            AppUserRepository appUserRepository
    ) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, AuthenticatedUser authenticatedUser) {
        validateBookingDates(request);

        Room room = roomRepository.findByIdAndHotelIdAndStateIgnoreCase(request.roomId(), request.hotelId(), ACTIVE)
                .orElseThrow(() -> new BookingFailedException("Please try again!!"));

        boolean unavailable = bookingRepository.existsOverlappingBookingForRoom(
                room.getId(),
                request.checkInDate(),
                request.checkOutDate(),
                BookingStatus.CONFIRMED
        );
        if (unavailable) {
            throw new BookingFailedException("Please try again!!");
        }

        AppUser appUser = appUserRepository.findByEmail(authenticatedUser.email())
                .orElseThrow(() -> new BookingFailedException("Please try again!!"));

        long totalNights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(totalNights));

        Booking booking = new Booking();
        booking.setUser(appUser);
        booking.setHotel(room.getHotel());
        booking.setRoom(room);
        booking.setBookingDate(OffsetDateTime.now());
        booking.setCheckInDate(request.checkInDate());
        booking.setCheckOutDate(request.checkOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        room.setBooked(true);
        room.setBookedDate(request.checkInDate());
        roomRepository.save(room);

        return new BookingResponse(
                savedBooking.getId(),
                "Your booking for the Hotel " + room.getHotel().getName() + " is successful",
                room.getHotel().getName(),
                room.getRoomLabel(),
                savedBooking.getCheckInDate(),
                savedBooking.getCheckOutDate(),
                savedBooking.getTotalPrice(),
                savedBooking.getStatus(),
                savedBooking.getBookingDate()
        );
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, AuthenticatedUser authenticatedUser) {
        Booking booking = bookingRepository.findByIdAndUserEmail(bookingId, authenticatedUser.email())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingFailedException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        updateRoomAvailabilityAfterCancellation(booking.getRoom());

        return new BookingResponse(
                booking.getId(),
                "Your booking for room " + booking.getRoom().getRoomLabel() + " has been cancelled",
                booking.getHotel().getName(),
                booking.getRoom().getRoomLabel(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getBookingDate()
        );
    }

    @Transactional(readOnly = true)
    public List<UserBookingHistoryResponse> getUserBookingHistory(String email) {
        return bookingRepository.findByUserEmailOrderByBookingDateDesc(email).stream()
                .map(booking -> new UserBookingHistoryResponse(
                        booking.getId(),
                        booking.getHotel().getName(),
                        booking.getHotel().getCity(),
                        booking.getHotel().getState(),
                        booking.getRoom().getRoomLabel(),
                        booking.getRoom().getRoomType(),
                        booking.getBookingDate(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getTotalPrice(),
                        booking.getStatus()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminBookingView> getAllBookingsForAdmin(
            BookingStatus status,
            LocalDate checkInDateFrom,
            LocalDate checkOutDateTo
    ) {
        return bookingRepository.findAdminBookings(status, checkInDateFrom, checkOutDateTo);
    }

    private void validateBookingDates(BookingRequest request) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new BookingFailedException("Please try again!!");
        }
    }

    private void updateRoomAvailabilityAfterCancellation(Room room) {
        Optional<Booking> nextBooking = bookingRepository
                .findFirstByRoomIdAndStatusAndCheckOutDateGreaterThanEqualOrderByCheckInDateAsc(
                        room.getId(),
                        BookingStatus.CONFIRMED,
                        LocalDate.now()
                );

        if (nextBooking.isPresent()) {
            room.setBooked(true);
            room.setBookedDate(nextBooking.get().getCheckInDate());
        } else {
            room.setBooked(false);
            room.setBookedDate(null);
        }

        roomRepository.save(room);
    }
}
