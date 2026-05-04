package com.example.Summit_Project.booking.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        List<String> details
) {
}
