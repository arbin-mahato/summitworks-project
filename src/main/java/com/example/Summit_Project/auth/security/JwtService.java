package com.example.Summit_Project.auth.security;

import com.example.Summit_Project.config.JwtProperties;
import com.example.Summit_Project.auth.entity.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private static final int MIN_HS256_KEY_BYTES = 32;

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(AppUser appUser) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(appUser.getUsername())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .claim("role", appUser.getRole().name())
                .signWith(signingKey())
                .compact();
    }

    public Instant getExpiryTime() {
        return Instant.now().plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES);
    }

    private SecretKey signingKey() {
        byte[] secretBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_HS256_KEY_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 bytes long for HS256. " +
                            "Provide a longer random secret in your environment configuration."
            );
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
