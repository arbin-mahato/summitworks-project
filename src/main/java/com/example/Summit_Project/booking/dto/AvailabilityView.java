package com.example.Summit_Project.booking.dto;

import java.time.LocalDate;

public record AvailabilityView(
        LocalDate date,
        boolean available,
        long availableRooms
) {
}
