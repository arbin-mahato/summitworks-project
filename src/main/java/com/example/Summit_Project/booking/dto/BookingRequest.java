package com.example.Summit_Project.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull(message = "hotelId is required")
        Long hotelId,
        @NotNull(message = "roomId is required")
        Long roomId,
        @NotNull(message = "checkInDate is required")
        @FutureOrPresent(message = "checkInDate must be today or in the future")
        LocalDate checkInDate,
        @NotNull(message = "checkOutDate is required")
        @FutureOrPresent(message = "checkOutDate must be today or in the future")
        LocalDate checkOutDate
) {
}
