package com.example.Summit_Project.booking.controller;

import com.example.Summit_Project.booking.dto.AdminBookingView;
import com.example.Summit_Project.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<AdminBookingView>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsForAdmin());
    }
}
