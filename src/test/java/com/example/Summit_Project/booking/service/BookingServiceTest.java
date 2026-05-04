package com.example.Summit_Project.booking.service;

import com.example.Summit_Project.auth.entity.AppUser;
import com.example.Summit_Project.auth.repository.AppUserRepository;
import com.example.Summit_Project.booking.dto.BookingRequest;
import com.example.Summit_Project.booking.entity.Booking;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.entity.Hotel;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.exception.BookingFailedException;
import com.example.Summit_Project.booking.repository.BookingRepository;
import com.example.Summit_Project.booking.repository.RoomRepository;
import com.example.Summit_Project.booking.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldCreateBookingForAvailableRoom() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Summit Hotel");
        hotel.setCity("San Diego");
        hotel.setState("California");

        Room room = new Room();
        room.setId(10L);
        room.setHotel(hotel);
        room.setRoomLabel("GS-101");
        room.setRoomType("Deluxe King");
        room.setState("ACTIVE");
        room.setPrice(new BigDecimal("159.99"));

        AppUser appUser = new AppUser();
        appUser.setId(100L);
        appUser.setUsername("user");

        when(roomRepository.findByIdAndHotelIdAndStateIgnoreCase(10L, 1L, "ACTIVE")).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBookingForRoom(
                10L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                BookingStatus.CONFIRMED
        )).thenReturn(false);
        when(appUserRepository.findByUsername("user")).thenReturn(Optional.of(appUser));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(900L);
            return booking;
        });

        var response = bookingService.createBooking(
                new BookingRequest(1L, 10L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)),
                new AuthenticatedUser("user", "USER")
        );

        assertThat(response.bookingId()).isEqualTo(900L);
        assertThat(response.hotelName()).isEqualTo("Grand Summit Hotel");
        assertThat(response.roomLabel()).isEqualTo("GS-101");
        assertThat(response.totalPrice()).isEqualByComparingTo("319.98");
        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void shouldRejectBookingWhenRoomAlreadyBookedForDateRange() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Summit Hotel");

        Room room = new Room();
        room.setId(10L);
        room.setHotel(hotel);
        room.setRoomLabel("GS-101");
        room.setState("ACTIVE");
        room.setPrice(new BigDecimal("159.99"));

        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(7);

        when(roomRepository.findByIdAndHotelIdAndStateIgnoreCase(10L, 1L, "ACTIVE")).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBookingForRoom(10L, checkIn, checkOut, BookingStatus.CONFIRMED))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(
                new BookingRequest(1L, 10L, checkIn, checkOut),
                new AuthenticatedUser("user", "USER")
        )).isInstanceOf(BookingFailedException.class);
    }

    @Test
    void shouldRejectBookingWhenRoomDoesNotBelongToHotel() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(7);

        when(roomRepository.findByIdAndHotelIdAndStateIgnoreCase(10L, 99L, "ACTIVE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(
                new BookingRequest(99L, 10L, checkIn, checkOut),
                new AuthenticatedUser("user", "USER")
        )).isInstanceOf(BookingFailedException.class);
    }
}
