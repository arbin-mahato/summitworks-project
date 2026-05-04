package com.example.Summit_Project.booking.dto;

import com.example.Summit_Project.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record BookingResponse(
        Long bookingId,
        String message,
        String hotelName,
        String roomLabel,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice,
        BookingStatus status,
        OffsetDateTime bookingDate
) {
}
