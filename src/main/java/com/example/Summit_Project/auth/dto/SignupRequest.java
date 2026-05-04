package com.example.Summit_Project.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 80, message = "username must be between 3 and 80 characters")
        String username,
        @NotBlank(message = "password is required")
        @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "password must contain at least one letter and one number"
        )
        String password
) {
}
