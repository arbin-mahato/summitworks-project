package com.example.Summit_Project.booking.service;

import com.example.Summit_Project.booking.dto.RoomDetailResponse;
import com.example.Summit_Project.booking.dto.RoomSummaryResponse;
import com.example.Summit_Project.booking.entity.Booking;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.exception.BookingFailedException;
import com.example.Summit_Project.booking.repository.BookingRepository;
import com.example.Summit_Project.booking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private static final String ACTIVE = "ACTIVE";

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final HotelService hotelService;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository, HotelService hotelService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.hotelService = hotelService;
    }

    @Transactional(readOnly = true)
    public List<RoomSummaryResponse> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelIdAndStateIgnoreCaseOrderByIdAsc(hotelId, ACTIVE).stream()
                .map(room -> new RoomSummaryResponse(
                        room.getId(),
                        room.getRoomLabel(),
                        room.getRoomType(),
                        room.getPrice(),
                        splitFeatures(room.getFeatures()),
                        true
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomSummaryResponse> getAvailableRoomsByHotel(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        validateDateRange(checkInDate, checkOutDate);

        List<Room> rooms = roomRepository.findByHotelIdAndStateIgnoreCaseOrderByIdAsc(hotelId, ACTIVE);
        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        Map<Long, List<Booking>> bookingsByRoomId = loadBookingsByRoom(roomIds, checkInDate, checkOutDate);

        return rooms.stream()
                .map(room -> {
                    boolean available = hotelService.isRoomAvailable(room, checkInDate, checkOutDate, bookingsByRoomId);
                    return new RoomSummaryResponse(
                            room.getId(),
                            room.getRoomLabel(),
                            room.getRoomType(),
                            room.getPrice(),
                            splitFeatures(room.getFeatures()),
                            available
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomDetails(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        validateDateRange(checkInDate, checkOutDate);

        Room room = roomRepository.findByIdAndStateIgnoreCase(roomId, ACTIVE)
                .orElseThrow(() -> new BookingFailedException("Please try again!!"));

        Map<Long, List<Booking>> bookingsByRoomId = loadBookingsByRoom(
                List.of(room.getId()),
                checkInDate,
                checkOutDate
        );
        boolean available = hotelService.isRoomAvailable(room, checkInDate, checkOutDate, bookingsByRoomId);

        return new RoomDetailResponse(
                room.getId(),
                room.getHotel().getId(),
                room.getHotel().getName(),
                room.getRoomLabel(),
                room.getRoomType(),
                room.getDescription(),
                splitFeatures(room.getFeatures()),
                room.getPrice(),
                available
        );
    }

    private void validateDateRange(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            throw new BookingFailedException("Please try again!!");
        }
    }

    private Map<Long, List<Booking>> loadBookingsByRoom(List<Long> roomIds, LocalDate checkInDate, LocalDate checkOutDate) {
        if (roomIds.isEmpty()) {
            return Map.of();
        }
        return bookingRepository.findOverlappingBookingsForRooms(roomIds, checkInDate, checkOutDate, BookingStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(booking -> booking.getRoom().getId()));
    }

    private List<String> splitFeatures(String features) {
        if (features == null || features.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(features.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
