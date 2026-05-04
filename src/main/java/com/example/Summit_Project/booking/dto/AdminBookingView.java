package com.example.Summit_Project.booking.dto;

import com.example.Summit_Project.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AdminBookingView(
        Long bookingId,
        String username,
        String hotelName,
        String city,
        String state,
        String roomLabel,
        String roomType,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice,
        BookingStatus status,
        OffsetDateTime bookingDate
) {
}
