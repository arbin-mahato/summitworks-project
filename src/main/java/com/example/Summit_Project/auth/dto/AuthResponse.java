package com.example.Summit_Project.auth.dto;

import com.example.Summit_Project.auth.entity.Role;

import java.time.Instant;

public record AuthResponse(
        String token,
        String tokenType,
        Instant expiresAt,
        Role role
) {
}
