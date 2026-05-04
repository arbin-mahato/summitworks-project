package com.example.Summit_Project.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminCreateHotelRequest(
        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,
        @NotBlank(message = "state is required")
        @Size(max = 30, message = "state must be at most 30 characters")
        String state,
        @NotBlank(message = "city is required")
        @Size(max = 80, message = "city must be at most 80 characters")
        String city,
        @Size(max = 255, message = "description must be at most 255 characters")
        String description,
        @NotNull(message = "pricePerNight is required")
        @DecimalMin(value = "0.01", message = "pricePerNight must be greater than 0")
        BigDecimal pricePerNight
) {
}
