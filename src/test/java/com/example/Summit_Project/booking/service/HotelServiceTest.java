package com.example.Summit_Project.booking.service;

import com.example.Summit_Project.booking.dto.HotelResponse;
import com.example.Summit_Project.booking.dto.HotelCalendarResponse;
import com.example.Summit_Project.booking.entity.Booking;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.entity.Hotel;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.repository.BookingRepository;
import com.example.Summit_Project.booking.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void shouldBuildSevenDayCalendarUsingConfirmedBookingOverlapLogic() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Summit Hotel");
        hotel.setState("California");
        hotel.setCity("San Diego");
        hotel.setDescription("Luxury business hotel near the harbor.");
        hotel.setPricePerNight(new BigDecimal("159.99"));

        Room roomOne = buildRoom(1L, hotel, "GS-101", "ACTIVE", "159.99");
        Room roomTwo = buildRoom(2L, hotel, "GS-102", "ACTIVE", "149.99");
        Room inactiveRoom = buildRoom(3L, hotel, "GS-999", "INACTIVE", "99.99");
        hotel.setRooms(List.of(roomOne, roomTwo, inactiveRoom));

        Booking booking = new Booking();
        booking.setRoom(roomOne);
        booking.setCheckInDate(LocalDate.of(2026, 5, 10));
        booking.setCheckOutDate(LocalDate.of(2026, 5, 12));
        booking.setStatus(BookingStatus.CONFIRMED);

        when(hotelRepository.findAllByOrderByNameAsc()).thenReturn(List.of(hotel));
        when(bookingRepository.findOverlappingBookingsForRooms(
                List.of(1L, 2L),
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 17),
                BookingStatus.CONFIRMED
        )).thenReturn(List.of(booking));

        List<HotelResponse> response = hotelService.getHotels(null, null, LocalDate.of(2026, 5, 10), 7);

        assertThat(response).hasSize(1);
        HotelResponse hotelResponse = response.get(0);
        assertThat(hotelResponse.totalRooms()).isEqualTo(2);
        assertThat(hotelResponse.startingPrice()).isEqualByComparingTo("149.99");
        assertThat(hotelResponse.calendar()).hasSize(7);
        assertThat(hotelResponse.calendar().get(0).date()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(hotelResponse.calendar().get(0).available()).isTrue();
        assertThat(hotelResponse.calendar().get(0).availableRooms()).isEqualTo(1);
        assertThat(hotelResponse.calendar().get(1).date()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(hotelResponse.calendar().get(1).available()).isTrue();
        assertThat(hotelResponse.calendar().get(1).availableRooms()).isEqualTo(1);
        assertThat(hotelResponse.calendar().get(2).date()).isEqualTo(LocalDate.of(2026, 5, 12));
        assertThat(hotelResponse.calendar().get(2).available()).isTrue();
        assertThat(hotelResponse.calendar().get(2).availableRooms()).isEqualTo(2);
    }

    @Test
    void shouldMarkDayUnavailableWhenAllActiveRoomsAreBooked() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Summit Hotel");
        hotel.setState("California");
        hotel.setCity("San Diego");
        hotel.setPricePerNight(new BigDecimal("159.99"));

        Room roomOne = buildRoom(1L, hotel, "GS-101", "ACTIVE", "159.99");
        Room roomTwo = buildRoom(2L, hotel, "GS-102", "ACTIVE", "149.99");
        hotel.setRooms(List.of(roomOne, roomTwo));

        Booking bookingOne = new Booking();
        bookingOne.setRoom(roomOne);
        bookingOne.setCheckInDate(LocalDate.of(2026, 5, 10));
        bookingOne.setCheckOutDate(LocalDate.of(2026, 5, 11));
        bookingOne.setStatus(BookingStatus.CONFIRMED);

        Booking bookingTwo = new Booking();
        bookingTwo.setRoom(roomTwo);
        bookingTwo.setCheckInDate(LocalDate.of(2026, 5, 10));
        bookingTwo.setCheckOutDate(LocalDate.of(2026, 5, 11));
        bookingTwo.setStatus(BookingStatus.CONFIRMED);

        when(hotelRepository.findAllByOrderByNameAsc()).thenReturn(List.of(hotel));
        when(bookingRepository.findOverlappingBookingsForRooms(
                List.of(1L, 2L),
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 11),
                BookingStatus.CONFIRMED
        )).thenReturn(List.of(bookingOne, bookingTwo));

        List<HotelResponse> response = hotelService.getHotels(null, null, LocalDate.of(2026, 5, 10), 1);

        assertThat(response.get(0).calendar()).hasSize(1);
        assertThat(response.get(0).calendar().get(0).available()).isFalse();
        assertThat(response.get(0).calendar().get(0).availableRooms()).isZero();
    }

    @Test
    void shouldReturnCalendarForSingleHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Summit Hotel");
        hotel.setState("California");
        hotel.setCity("San Diego");
        hotel.setPricePerNight(new BigDecimal("159.99"));

        Room roomOne = buildRoom(1L, hotel, "GS-101", "ACTIVE", "159.99");
        Room roomTwo = buildRoom(2L, hotel, "GS-102", "ACTIVE", "149.99");
        hotel.setRooms(List.of(roomOne, roomTwo));

        Booking booking = new Booking();
        booking.setRoom(roomOne);
        booking.setCheckInDate(LocalDate.of(2026, 5, 10));
        booking.setCheckOutDate(LocalDate.of(2026, 5, 12));
        booking.setStatus(BookingStatus.CONFIRMED);

        when(hotelRepository.findById(1L)).thenReturn(java.util.Optional.of(hotel));
        when(bookingRepository.findOverlappingBookingsForRooms(
                List.of(1L, 2L),
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 13),
                BookingStatus.CONFIRMED
        )).thenReturn(List.of(booking));

        HotelCalendarResponse response = hotelService.getHotelCalendar(1L, LocalDate.of(2026, 5, 10), 3);

        assertThat(response.hotelId()).isEqualTo(1L);
        assertThat(response.hotelName()).isEqualTo("Grand Summit Hotel");
        assertThat(response.totalRooms()).isEqualTo(2);
        assertThat(response.calendar()).hasSize(3);
        assertThat(response.calendar().get(0).availableRooms()).isEqualTo(1);
        assertThat(response.calendar().get(1).availableRooms()).isEqualTo(1);
        assertThat(response.calendar().get(2).availableRooms()).isEqualTo(2);
    }

    private Room buildRoom(Long id, Hotel hotel, String roomLabel, String state, String price) {
        Room room = new Room();
        room.setId(id);
        room.setHotel(hotel);
        room.setRoomLabel(roomLabel);
        room.setState(state);
        room.setPrice(new BigDecimal(price));
        return room;
    }
}
