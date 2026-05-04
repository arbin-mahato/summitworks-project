package com.example.Summit_Project.booking.dto;

import java.math.BigDecimal;

public record AdminHotelResponse(
        Long hotelId,
        String name,
        String state,
        String city,
        String description,
        BigDecimal pricePerNight,
        String message
) {
}
