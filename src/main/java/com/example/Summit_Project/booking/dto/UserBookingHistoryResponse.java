package com.example.Summit_Project.booking.dto;

import com.example.Summit_Project.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record UserBookingHistoryResponse(
        Long bookingId,
        String hotelName,
        String city,
        String state,
        String roomLabel,
        String roomType,
        OffsetDateTime bookingDate,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice,
        BookingStatus status
) {
}
