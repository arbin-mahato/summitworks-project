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
        appUser.setUsername("user");
        appUser.setPasswordHash("encoded");
        appUser.setRole(Role.USER);

        when(appUserRepository.findByUsername("user")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.matches("User@123", "encoded")).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn("jwt-token");
        when(jwtService.getExpiryTime()).thenReturn(java.time.Instant.parse("2030-01-01T00:00:00Z"));

        var response = authService.authenticate(new AuthRequest("user", "User@123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void shouldRejectInvalidPassword() {
        AppUser appUser = new AppUser();
        appUser.setUsername("user");
        appUser.setPasswordHash("encoded");

        when(appUserRepository.findByUsername("user")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(new AuthRequest("user", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldSignupNewUser() {
        AppUser savedUser = new AppUser();
        savedUser.setUsername("Arbin");
        savedUser.setPasswordHash("encoded");
        savedUser.setRole(Role.USER);

        when(appUserRepository.existsByUsername("Arbin")).thenReturn(false);
        when(passwordEncoder.encode("abc123")).thenReturn("encoded");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("signup-token");
        when(jwtService.getExpiryTime()).thenReturn(java.time.Instant.parse("2030-01-01T00:00:00Z"));

        var response = authService.signup(new SignupRequest("Arbin", "abc123"));

        assertThat(response.token()).isEqualTo("signup-token");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void shouldRejectDuplicateUsernameOnSignup() {
        when(appUserRepository.existsByUsername("user")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(new SignupRequest("user", "abc123")))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void shouldAssignAdminRoleToExistingUser() {
        AppUser appUser = new AppUser();
        appUser.setUsername("Arbin");
        appUser.setRole(Role.USER);

        when(appUserRepository.findByUsername("Arbin")).thenReturn(Optional.of(appUser));
        when(appUserRepository.save(appUser)).thenReturn(appUser);

        var response = authService.assignRole("Arbin", new RoleAssignmentRequest(Role.ADMIN));

        assertThat(response.username()).isEqualTo("Arbin");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldFailAssignRoleWhenUserMissing() {
        when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.assignRole("ghost", new RoleAssignmentRequest(Role.ADMIN)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
