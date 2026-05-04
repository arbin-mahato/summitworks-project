package com.example.Summit_Project.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank(message = "JWT secret must be provided via the JWT_SECRET environment variable")
        String secret,
        @Min(value = 1, message = "JWT expiration minutes must be greater than 0")
        long expirationMinutes,
        @NotBlank(message = "JWT issuer must not be blank")
        String issuer
) {
}
