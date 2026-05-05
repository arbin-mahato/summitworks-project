package com.example.Summit_Project.booking.controller;

import com.example.Summit_Project.booking.dto.UserBookingHistoryResponse;
import com.example.Summit_Project.booking.security.AuthenticatedUser;
import com.example.Summit_Project.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/bookings")
public class UserBookingController {

    private final BookingService bookingService;

    public UserBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<UserBookingHistoryResponse>> getMyBookings(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(bookingService.getUserBookingHistory(authenticatedUser.email()));
    }
}
