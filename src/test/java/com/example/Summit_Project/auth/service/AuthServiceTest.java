package com.example.Summit_Project.auth.service;

import com.example.Summit_Project.auth.dto.AuthRequest;
import com.example.Summit_Project.auth.dto.RoleAssignmentRequest;
import com.example.Summit_Project.auth.dto.SignupRequest;
import com.example.Summit_Project.auth.entity.AppUser;
import com.example.Summit_Project.auth.entity.Role;
import com.example.Summit_Project.auth.exception.UserAlreadyExistsException;
import com.example.Summit_Project.auth.exception.UserNotFoundException;
import com.example.Summit_Project.auth.repository.AppUserRepository;
import com.example.Summit_Project.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldAuthenticateValidUser() {
        AppUser appUser = new AppUser();
        appUser.setFullName("Test User");
        appUser.setEmail("user@example.com");
        appUser.setPasswordHash("encoded");
        appUser.setRole(Role.USER);

        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.matches("User@123", "encoded")).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn("jwt-token");
        when(jwtService.getExpiryTime()).thenReturn(java.time.Instant.parse("2030-01-01T00:00:00Z"));

        var response = authService.authenticate(new AuthRequest("user@example.com", "User@123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void shouldRejectInvalidPassword() {
        AppUser appUser = new AppUser();
        appUser.setFullName("Test User");
        appUser.setEmail("user@example.com");
        appUser.setPasswordHash("encoded");

        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("user@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldSignupNewUser() {
        AppUser savedUser = new AppUser();
        savedUser.setFullName("Arbin");
        savedUser.setEmail("arbin@example.com");
        savedUser.setPasswordHash("encoded");
        savedUser.setRole(Role.USER);

        when(appUserRepository.existsByEmail("arbin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("abc123")).thenReturn("encoded");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("signup-token");
        when(jwtService.getExpiryTime()).thenReturn(java.time.Instant.parse("2030-01-01T00:00:00Z"));

        var response = authService.signup(new SignupRequest("Arbin", "arbin@example.com", "abc123"));

        assertThat(response.token()).isEqualTo("signup-token");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void shouldRejectDuplicateEmailOnSignup() {
        when(appUserRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(new SignupRequest("User", "user@example.com", "abc123")))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void shouldAssignAdminRoleToExistingUser() {
        AppUser appUser = new AppUser();
        appUser.setFullName("Arbin");
        appUser.setEmail("arbin@example.com");
        appUser.setRole(Role.USER);

        when(appUserRepository.findByEmail("arbin@example.com")).thenReturn(Optional.of(appUser));
        when(appUserRepository.save(appUser)).thenReturn(appUser);

        var response = authService.assignRole("arbin@example.com", new RoleAssignmentRequest(Role.ADMIN));

        assertThat(response.email()).isEqualTo("arbin@example.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldFailAssignRoleWhenUserMissing() {
        when(appUserRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.assignRole("ghost@example.com", new RoleAssignmentRequest(Role.ADMIN)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
