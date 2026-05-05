package com.example.Summit_Project.booking.service;

import com.example.Summit_Project.booking.dto.RoomSummaryResponse;
import com.example.Summit_Project.booking.entity.Booking;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.entity.Hotel;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.exception.BookingFailedException;
import com.example.Summit_Project.booking.repository.BookingRepository;
import com.example.Summit_Project.booking.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private RoomService roomService;

    @Test
    void shouldListAllRoomsForHotelWithoutDateFilter() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        Room room = new Room();
        room.setId(2L);
        room.setHotel(hotel);
        room.setRoomLabel("GS-102");
        room.setRoomType("Twin Deluxe");
        room.setState("ACTIVE");
        room.setFeatures("WiFi,Work Desk");
        room.setPrice(new BigDecimal("149.99"));

        when(roomRepository.findByHotelIdAndStateIgnoreCaseOrderByIdAsc(1L, "ACTIVE")).thenReturn(List.of(room));

        List<RoomSummaryResponse> response = roomService.getRoomsByHotel(1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).roomId()).isEqualTo(2L);
        assertThat(response.get(0).roomLabel()).isEqualTo("GS-102");
        assertThat(response.get(0).available()).isTrue();
    }

    @Test
    void shouldMarkBookedRoomUnavailableForRequestedDateRange() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Summit Hotel");

        Room room = new Room();
        room.setId(2L);
        room.setHotel(hotel);
        room.setRoomLabel("GS-102");
        room.setRoomType("Twin Deluxe");
        room.setState("ACTIVE");
        room.setFeatures("WiFi,Work Desk,Coffee Maker");
        room.setPrice(new BigDecimal("149.99"));

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCheckInDate(LocalDate.of(2026, 5, 10));
        booking.setCheckOutDate(LocalDate.of(2026, 5, 12));
        booking.setStatus(BookingStatus.CONFIRMED);

        when(roomRepository.findByHotelIdAndStateIgnoreCaseOrderByIdAsc(1L, "ACTIVE")).thenReturn(List.of(room));
        when(bookingRepository.findOverlappingBookingsForRooms(
                List.of(2L),
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                BookingStatus.CONFIRMED
        )).thenReturn(List.of(booking));
        when(hotelService.isRoomAvailable(
                room,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                java.util.Map.of(2L, List.of(booking))
        )).thenReturn(false);

        List<RoomSummaryResponse> response = roomService.getAvailableRoomsByHotel(
                1L,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12)
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).roomId()).isEqualTo(2L);
        assertThat(response.get(0).available()).isFalse();
    }

    @Test
    void shouldRejectRoomAvailabilityLookupWhenDateRangeIsInvalid() {
        assertThatThrownBy(() -> roomService.getAvailableRoomsByHotel(
                1L,
                LocalDate.of(2026, 5, 12),
                LocalDate.of(2026, 5, 10)
        )).isInstanceOf(BookingFailedException.class);
    }
}
