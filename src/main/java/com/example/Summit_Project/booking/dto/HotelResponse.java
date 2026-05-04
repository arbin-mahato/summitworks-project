package com.example.Summit_Project.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public record HotelResponse(
        Long hotelId,
        String hotelName,
        String state,
        String city,
        String description,
        BigDecimal startingPrice,
        long totalRooms,
        long availableRooms,
        List<AvailabilityView> calendar
) {
}
