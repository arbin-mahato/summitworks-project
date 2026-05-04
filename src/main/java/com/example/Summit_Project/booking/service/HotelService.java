package com.example.Summit_Project.booking.service;

import com.example.Summit_Project.booking.dto.AvailabilityView;
import com.example.Summit_Project.booking.dto.HotelCalendarResponse;
import com.example.Summit_Project.booking.dto.HotelResponse;
import com.example.Summit_Project.booking.entity.Booking;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.entity.Hotel;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.exception.BookingFailedException;
import com.example.Summit_Project.booking.repository.BookingRepository;
import com.example.Summit_Project.booking.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HotelService {
    private static final String ACTIVE = "ACTIVE";

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    public HotelService(HotelRepository hotelRepository, BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> getHotels(String state, String city, LocalDate startDate, int days) {
        LocalDate effectiveStartDate = startDate == null ? LocalDate.now() : startDate;
        List<Hotel> hotels = loadHotels(state, city);
        Map<Long, List<Booking>> bookingsByRoomId = loadBookingsByRoomId(hotels, effectiveStartDate, days);

        return hotels.stream()
                .map(hotel -> buildHotelResponse(hotel, effectiveStartDate, days, bookingsByRoomId))
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelCalendarResponse getHotelCalendar(Long hotelId, LocalDate startDate, int days) {
        LocalDate effectiveStartDate = startDate == null ? LocalDate.now() : startDate;
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new BookingFailedException("Please try again!!"));
        Map<Long, List<Booking>> bookingsByRoomId = loadBookingsByRoomId(List.of(hotel), effectiveStartDate, days);
        List<Room> activeRooms = hotel.getRooms().stream()
                .filter(room -> ACTIVE.equalsIgnoreCase(room.getState()))
                .toList();

        return new HotelCalendarResponse(
                hotel.getId(),
                hotel.getName(),
                activeRooms.size(),
                buildCalendar(activeRooms, effectiveStartDate, days, bookingsByRoomId)
        );
    }

    private List<Hotel> loadHotels(String state, String city) {
        boolean hasState = state != null && !state.isBlank();
        boolean hasCity = city != null && !city.isBlank();

        if (hasState && hasCity) {
            return hotelRepository.findAllByStateIgnoreCaseAndCityIgnoreCaseOrderByNameAsc(state, city);
        }
        if (hasState) {
            return hotelRepository.findAllByStateIgnoreCaseOrderByNameAsc(state);
        }
        if (hasCity) {
            return hotelRepository.findAllByCityIgnoreCaseOrderByNameAsc(city);
        }
        return hotelRepository.findAllByOrderByNameAsc();
    }

    private HotelResponse buildHotelResponse(
            Hotel hotel,
            LocalDate startDate,
            int days,
            Map<Long, List<Booking>> bookingsByRoomId
    ) {
        List<Room> activeRooms = hotel.getRooms().stream()
                .filter(room -> ACTIVE.equalsIgnoreCase(room.getState()))
                .toList();

        long totalRooms = activeRooms.size();
        List<AvailabilityView> calendar = buildCalendar(activeRooms, startDate, days, bookingsByRoomId);
        long maxAvailableRooms = calendar.stream()
                .mapToLong(AvailabilityView::availableRooms)
                .max()
                .orElse(0L);

        BigDecimal startingPrice = activeRooms.stream()
                .map(Room::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(hotel.getPricePerNight());

        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getState(),
                hotel.getCity(),
                hotel.getDescription(),
                startingPrice,
                totalRooms,
                maxAvailableRooms,
                calendar
        );
    }

    private Map<Long, List<Booking>> loadBookingsByRoomId(List<Hotel> hotels, LocalDate startDate, int days) {
        List<Long> roomIds = hotels.stream()
                .flatMap(hotel -> hotel.getRooms().stream())
                .filter(room -> ACTIVE.equalsIgnoreCase(room.getState()))
                .map(Room::getId)
                .toList();

        LocalDate endDate = startDate.plusDays(days);
        List<Booking> overlappingBookings = roomIds.isEmpty()
                ? List.of()
                : bookingRepository.findOverlappingBookingsForRooms(
                roomIds,
                startDate,
                endDate,
                BookingStatus.CONFIRMED
        );

        return overlappingBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getRoom().getId()));
    }

    private List<AvailabilityView> buildCalendar(
            List<Room> activeRooms,
            LocalDate startDate,
            int days,
            Map<Long, List<Booking>> bookingsByRoomId
    ) {
        List<AvailabilityView> calendar = new ArrayList<>();
        for (int offset = 0; offset < days; offset++) {
            LocalDate date = startDate.plusDays(offset);
            long availableRooms = activeRooms.stream()
                    .filter(room -> isRoomAvailable(room, date, date.plusDays(1), bookingsByRoomId))
                    .count();
            calendar.add(new AvailabilityView(date, availableRooms > 0, availableRooms));
        }
        return calendar;
    }

    boolean isRoomAvailable(Room room, LocalDate checkInDate, LocalDate checkOutDate, Map<Long, List<Booking>> bookingsByRoomId) {
        return bookingsByRoomId.getOrDefault(room.getId(), List.of()).stream()
                .noneMatch(booking -> overlaps(booking.getCheckInDate(), booking.getCheckOutDate(), checkInDate, checkOutDate));
    }

    private boolean overlaps(LocalDate existingStart, LocalDate existingEnd, LocalDate requestedStart, LocalDate requestedEnd) {
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }
}
