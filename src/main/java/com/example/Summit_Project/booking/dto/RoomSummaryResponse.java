package com.example.Summit_Project.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public record RoomSummaryResponse(
        Long roomId,
        String roomLabel,
        String roomType,
        BigDecimal price,
        List<String> features,
        boolean available
) {
}
