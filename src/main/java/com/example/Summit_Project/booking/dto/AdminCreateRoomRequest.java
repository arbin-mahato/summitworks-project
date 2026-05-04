package com.example.Summit_Project.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminCreateRoomRequest(
        @NotBlank(message = "roomLabel is required")
        @Size(max = 40, message = "roomLabel must be at most 40 characters")
        String roomLabel,
        @NotBlank(message = "roomType is required")
        @Size(max = 50, message = "roomType must be at most 50 characters")
        String roomType,
        @Size(max = 500, message = "description must be at most 500 characters")
        String description,
        @Size(max = 500, message = "features must be at most 500 characters")
        String features,
        @NotBlank(message = "state is required")
        @Size(max = 30, message = "state must be at most 30 characters")
        String state,
        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be greater than 0")
        BigDecimal price
) {
}
