package com.example.Summit_Project.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record AuthRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,
        @NotBlank(message = "password is required")
        String password
) {
}
