package com.example.Summit_Project.booking.service;

import com.example.Summit_Project.booking.dto.AdminCreateHotelRequest;
import com.example.Summit_Project.booking.dto.AdminCreateRoomRequest;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.entity.Hotel;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.exception.AdminOperationException;
import com.example.Summit_Project.booking.repository.BookingRepository;
import com.example.Summit_Project.booking.repository.HotelRepository;
import com.example.Summit_Project.booking.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInventoryServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AdminInventoryService adminInventoryService;

    @Test
    void shouldCreateHotelWhenNameIsUnique() {
        when(hotelRepository.existsByNameIgnoreCase("Grand Summit Hotel")).thenReturn(false);
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> {
            Hotel hotel = invocation.getArgument(0);
            hotel.setId(1L);
            return hotel;
        });

        var response = adminInventoryService.createHotel(
                new AdminCreateHotelRequest(
                        "Grand Summit Hotel",
                        "California",
                        "San Diego",
                        "Luxury business hotel near the harbor.",
                        new BigDecimal("159.99")
                )
        );

        assertThat(response.hotelId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Grand Summit Hotel");
        assertThat(response.message()).isEqualTo("Hotel created successfully");
    }

    @Test
    void shouldRejectHotelCreationWhenNameAlreadyExists() {
        when(hotelRepository.existsByNameIgnoreCase("Grand Summit Hotel")).thenReturn(true);

        assertThatThrownBy(() -> adminInventoryService.createHotel(
                new AdminCreateHotelRequest(
                        "Grand Summit Hotel",
                        "California",
                        "San Diego",
                        "Luxury business hotel near the harbor.",
                        new BigDecimal("159.99")
                )
        )).isInstanceOf(AdminOperationException.class);
    }

    @Test
    void shouldCreateRoomUnderExistingHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Summit Hotel");

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            room.setId(10L);
            return room;
        });

        var response = adminInventoryService.createRoom(
                1L,
                new AdminCreateRoomRequest(
                        "GS-301",
                        "Premium Suite",
                        "Top floor suite.",
                        "WiFi,Balcony,Mini Bar",
                        "ACTIVE",
                        new BigDecimal("249.99")
                )
        );

        assertThat(response.roomId()).isEqualTo(10L);
        assertThat(response.hotelId()).isEqualTo(1L);
        assertThat(response.booked()).isFalse();
        assertThat(response.bookedDate()).isNull();
        assertThat(response.message()).isEqualTo("Room created successfully");
    }

    @Test
    void shouldRejectRoomCreationWhenHotelDoesNotExist() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminInventoryService.createRoom(
                1L,
                new AdminCreateRoomRequest(
                        "GS-301",
                        "Premium Suite",
                        "Top floor suite.",
                        "WiFi,Balcony,Mini Bar",
                        "ACTIVE",
                        new BigDecimal("249.99")
                )
        )).isInstanceOf(AdminOperationException.class);
    }

    @Test
    void shouldRejectRoomDeletionWhenConfirmedCurrentOrFutureBookingExists() {
        Room room = new Room();
        room.setId(10L);

        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusAndCheckOutDateGreaterThanEqual(
                10L,
                BookingStatus.CONFIRMED,
                LocalDate.now()
        )).thenReturn(true);

        assertThatThrownBy(() -> adminInventoryService.deleteRoom(10L))
                .isInstanceOf(AdminOperationException.class);
    }

    @Test
    void shouldRejectRoomDeletionWhenHistoricalBookingHistoryBlocksDelete() {
        Room room = new Room();
        room.setId(10L);

        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusAndCheckOutDateGreaterThanEqual(
                10L,
                BookingStatus.CONFIRMED,
                LocalDate.now()
        )).thenReturn(false);
        doThrow(new DataIntegrityViolationException("fk")).when(roomRepository).flush();

        assertThatThrownBy(() -> adminInventoryService.deleteRoom(10L))
                .isInstanceOf(AdminOperationException.class);
    }
}
