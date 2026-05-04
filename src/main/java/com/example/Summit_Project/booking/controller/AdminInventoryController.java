package com.example.Summit_Project.booking.controller;

import com.example.Summit_Project.booking.dto.AdminActionResponse;
import com.example.Summit_Project.booking.dto.AdminCreateHotelRequest;
import com.example.Summit_Project.booking.dto.AdminCreateRoomRequest;
import com.example.Summit_Project.booking.dto.AdminHotelResponse;
import com.example.Summit_Project.booking.dto.AdminRoomResponse;
import com.example.Summit_Project.booking.service.AdminInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminInventoryController {

    private final AdminInventoryService adminInventoryService;

    public AdminInventoryController(AdminInventoryService adminInventoryService) {
        this.adminInventoryService = adminInventoryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/hotels")
    public ResponseEntity<AdminHotelResponse> createHotel(@Valid @RequestBody AdminCreateHotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminInventoryService.createHotel(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<AdminRoomResponse> createRoom(
            @PathVariable Long hotelId,
            @Valid @RequestBody AdminCreateRoomRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminInventoryService.createRoom(hotelId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<AdminActionResponse> deleteRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(adminInventoryService.deleteRoom(roomId));
    }
}
