package com.example.Summit_Project.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public record RoomDetailResponse(
        Long roomId,
        Long hotelId,
        String hotelName,
        String roomLabel,
        String roomType,
        String description,
        List<String> features,
        BigDecimal price,
        boolean available
) {
}
