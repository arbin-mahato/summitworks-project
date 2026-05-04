package com.example.Summit_Project.auth.service;

import com.example.Summit_Project.auth.dto.AuthRequest;
import com.example.Summit_Project.auth.dto.AuthResponse;
import com.example.Summit_Project.auth.dto.RoleAssignmentRequest;
import com.example.Summit_Project.auth.dto.SignupRequest;
import com.example.Summit_Project.auth.dto.UserRoleResponse;
import com.example.Summit_Project.auth.entity.AppUser;
import com.example.Summit_Project.auth.entity.Role;
import com.example.Summit_Project.auth.exception.UserAlreadyExistsException;
import com.example.Summit_Project.auth.exception.UserNotFoundException;
import com.example.Summit_Project.auth.repository.AppUserRepository;
import com.example.Summit_Project.auth.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse authenticate(AuthRequest request) {
        AppUser appUser = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), appUser.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(appUser);
        return new AuthResponse(token, "Bearer", jwtService.getExpiryTime(), appUser.getRole());
    }

    public AuthResponse signup(SignupRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username is already registered");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(request.username());
        appUser.setPasswordHash(passwordEncoder.encode(request.password()));
        appUser.setRole(Role.USER);

        AppUser savedUser = appUserRepository.save(appUser);
        String token = jwtService.generateToken(savedUser);
        return new AuthResponse(token, "Bearer", jwtService.getExpiryTime(), savedUser.getRole());
    }

    public UserRoleResponse assignRole(String username, RoleAssignmentRequest request) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        appUser.setRole(request.role());
        appUserRepository.save(appUser);

        return new UserRoleResponse(
                appUser.getUsername(),
                appUser.getRole(),
                "Role updated successfully. The user must log in again to receive a token with the new role."
        );
    }
}
