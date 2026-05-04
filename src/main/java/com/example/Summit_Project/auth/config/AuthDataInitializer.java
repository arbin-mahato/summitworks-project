package com.example.Summit_Project.auth.config;

import com.example.Summit_Project.auth.entity.AppUser;
import com.example.Summit_Project.auth.entity.Role;
import com.example.Summit_Project.auth.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("local")
public class AuthDataInitializer {

    @Bean
    CommandLineRunner seedUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(appUserRepository, passwordEncoder, "admin", "Admin@123", Role.ADMIN);
            createUserIfMissing(appUserRepository, passwordEncoder, "user", "User@123", Role.USER);
        };
    }

    private void createUserIfMissing(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            Role role
    ) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        appUser.setRole(role);
        appUserRepository.save(appUser);
    }
}
