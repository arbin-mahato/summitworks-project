package com.example.Summit_Project.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AdminRoomResponse(
        Long roomId,
        Long hotelId,
        String hotelName,
        String roomLabel,
        String roomType,
        String description,
        List<String> features,
        String state,
        boolean booked,
        LocalDate bookedDate,
        BigDecimal price,
        String message
) {
}
