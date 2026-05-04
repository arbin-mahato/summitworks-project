package com.example.Summit_Project.booking.service;

import com.example.Summit_Project.booking.dto.AdminActionResponse;
import com.example.Summit_Project.booking.dto.AdminCreateHotelRequest;
import com.example.Summit_Project.booking.dto.AdminCreateRoomRequest;
import com.example.Summit_Project.booking.dto.AdminHotelResponse;
import com.example.Summit_Project.booking.dto.AdminRoomResponse;
import com.example.Summit_Project.booking.entity.BookingStatus;
import com.example.Summit_Project.booking.entity.Hotel;
import com.example.Summit_Project.booking.entity.Room;
import com.example.Summit_Project.booking.exception.AdminOperationException;
import com.example.Summit_Project.booking.repository.BookingRepository;
import com.example.Summit_Project.booking.repository.HotelRepository;
import com.example.Summit_Project.booking.repository.RoomRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class AdminInventoryService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AdminInventoryService(
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository
    ) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public AdminHotelResponse createHotel(AdminCreateHotelRequest request) {
        if (hotelRepository.existsByNameIgnoreCase(request.name())) {
            throw new AdminOperationException("Hotel name must be unique");
        }

        Hotel hotel = new Hotel();
        hotel.setName(request.name().trim());
        hotel.setState(request.state().trim());
        hotel.setCity(request.city().trim());
        hotel.setDescription(request.description());
        hotel.setPricePerNight(request.pricePerNight());

        Hotel savedHotel = hotelRepository.save(hotel);
        return new AdminHotelResponse(
                savedHotel.getId(),
                savedHotel.getName(),
                savedHotel.getState(),
                savedHotel.getCity(),
                savedHotel.getDescription(),
                savedHotel.getPricePerNight(),
                "Hotel created successfully"
        );
    }

    @Transactional
    public AdminRoomResponse createRoom(Long hotelId, AdminCreateRoomRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AdminOperationException("Hotel not found"));

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomLabel(request.roomLabel().trim());
        room.setRoomType(request.roomType().trim());
        room.setDescription(request.description());
        room.setFeatures(request.features());
        room.setState(request.state().trim());
        room.setBooked(false);
        room.setBookedDate(null);
        room.setPrice(request.price());

        Room savedRoom = roomRepository.save(room);
        return new AdminRoomResponse(
                savedRoom.getId(),
                hotel.getId(),
                hotel.getName(),
                savedRoom.getRoomLabel(),
                savedRoom.getRoomType(),
                savedRoom.getDescription(),
                splitFeatures(savedRoom.getFeatures()),
                savedRoom.getState(),
                savedRoom.isBooked(),
                savedRoom.getBookedDate(),
                savedRoom.getPrice(),
                "Room created successfully"
        );
    }

    @Transactional
    public AdminActionResponse deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AdminOperationException("Room not found"));

        if (bookingRepository.existsByRoomIdAndStatusAndCheckOutDateGreaterThanEqual(
                roomId,
                BookingStatus.CONFIRMED,
                LocalDate.now()
        )) {
            throw new AdminOperationException("Room cannot be deleted because it has confirmed current or future bookings");
        }

        try {
            roomRepository.delete(room);
            roomRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new AdminOperationException("Room cannot be deleted because it has booking history");
        }

        return new AdminActionResponse("Room deleted successfully");
    }

    private List<String> splitFeatures(String features) {
        if (features == null || features.isBlank()) {
            return List.of();
        }
        return Arrays.stream(features.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
