package com.example.Summit_Project.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "fullName is required")
        @Size(min = 3, max = 80, message = "fullName must be between 3 and 80 characters")
        String fullName,
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 120, message = "email must not exceed 120 characters")
        String email,
        @NotBlank(message = "password is required")
        @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "password must contain at least one letter and one number"
        )
        String password
) {
}
