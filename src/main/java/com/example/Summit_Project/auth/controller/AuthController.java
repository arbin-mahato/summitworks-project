package com.example.Summit_Project.auth.controller;

import com.example.Summit_Project.auth.dto.AuthRequest;
import com.example.Summit_Project.auth.dto.AuthResponse;
import com.example.Summit_Project.auth.dto.SignupRequest;
import com.example.Summit_Project.auth.service.AuthService;
import com.example.Summit_Project.config.JwtProperties;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public static final String AUTH_COOKIE_NAME = "auth_token";

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService, JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok()
                .header("Set-Cookie", buildAuthCookie(response.token()).toString())
                .body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie", buildAuthCookie(response.token()).toString())
                .body(response);
    }

    private ResponseCookie buildAuthCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtProperties.expirationMinutes() * 60)
                .build();
    }
}
