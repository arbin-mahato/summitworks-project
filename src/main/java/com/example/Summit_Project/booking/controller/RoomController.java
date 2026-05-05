package com.example.Summit_Project.booking.controller;

import com.example.Summit_Project.booking.dto.RoomDetailResponse;
import com.example.Summit_Project.booking.dto.RoomSummaryResponse;
import com.example.Summit_Project.booking.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<List<RoomSummaryResponse>> getRoomsByHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate
    ) {
        if (checkInDate == null && checkOutDate == null) {
            return ResponseEntity.ok(roomService.getRoomsByHotel(hotelId));
        }
        return ResponseEntity.ok(roomService.getAvailableRoomsByHotel(hotelId, checkInDate, checkOutDate));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoomDetails(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate
    ) {
        return ResponseEntity.ok(roomService.getRoomDetails(roomId, checkInDate, checkOutDate));
    }
}
