package com.example.Summit_Project.auth.dto;

import com.example.Summit_Project.auth.entity.Role;

public record UserRoleResponse(
        String username,
        Role role,
        String message
) {
}
