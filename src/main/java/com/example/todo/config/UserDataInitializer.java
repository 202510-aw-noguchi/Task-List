package com.example.todo.config;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserDataInitializer {
    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            upsertSeedUser(userRepository, passwordEncoder,
                    "admin", "password", "ROLE_ADMIN", "admin@example.com");
            upsertSeedUser(userRepository, passwordEncoder,
                    "User_A", "password", "ROLE_USER", "user_a@example.com");
            upsertSeedUser(userRepository, passwordEncoder,
                    "User_B", "password", "ROLE_USER", "user_b@example.com");
            upsertSeedUser(userRepository, passwordEncoder,
                    "User_C", "password", "ROLE_USER", "user_c@example.com");
        };
    }

    private void upsertSeedUser(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                String username,
                                String rawPassword,
                                String role,
                                String email) {
        AppUser user = userRepository.findByUsername(username).orElseGet(AppUser::new);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEmail(email);
        user.setPasswordResetRequired(false);
        user.setPasswordResetIssuedAt(null);
        userRepository.save(user);
    }
}
