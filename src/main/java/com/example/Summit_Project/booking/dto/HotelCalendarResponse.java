package com.example.Summit_Project.booking.dto;

import java.util.List;

public record HotelCalendarResponse(
        Long hotelId,
        String hotelName,
        long totalRooms,
        List<AvailabilityView> calendar
) {
}
