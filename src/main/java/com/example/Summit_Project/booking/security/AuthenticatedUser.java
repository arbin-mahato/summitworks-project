package com.example.Summit_Project.booking.security;

import java.io.Serializable;

public record AuthenticatedUser(
        String email,
        String role
) implements Serializable {
}
