package com.example.Summit_Project.booking.security;

import java.io.Serializable;

public record AuthenticatedUser(
        String username,
        String role
) implements Serializable {
}
