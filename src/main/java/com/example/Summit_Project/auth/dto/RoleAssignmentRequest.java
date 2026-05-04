package com.example.Summit_Project.auth.dto;

import com.example.Summit_Project.auth.entity.Role;
import jakarta.validation.constraints.NotNull;

public record RoleAssignmentRequest(
        @NotNull(message = "role is required")
        Role role
) {
}
