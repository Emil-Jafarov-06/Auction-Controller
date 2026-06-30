package com.project.authservice.config;

import com.project.authservice.enums.Role;
import com.project.authservice.exception.ConflictException;
import com.project.authservice.exception.NotFoundException;
import com.project.authservice.model.entity.User;
import com.project.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${app.admin.bootstrap.fullname:System Admin}")
    private String fullName;

    @Value("${app.admin.bootstrap.email}")
    private String email;

    @Value("${app.admin.bootstrap.pin}")
    private String pin;

    @Value("${app.admin.bootstrap.password}")
    private String password;

    @Override
    public void run(String... args) throws Exception {
        if(!enabled || userRepository.existsByRoleAndDeletedFalse(Role.ADMIN)) {
            return;
        }
        if(email == null || email.isBlank() ||
                pin == null || pin.isBlank() ||
                password == null || password.isBlank()) {
            throw new NotFoundException("Admin configurations are incomplete!");
        }

        email = email.trim().toLowerCase();
        pin = pin.trim().toLowerCase();

        if(userRepository.existsByEmailAndDeletedFalse(email)) {
            throw new ConflictException("Email is in use!");
        }
        if(userRepository.existsByPinAndDeletedFalse(pin)){
            throw new ConflictException("Pin is in use!");
        }

        User admin = User.builder()
                .fullName(fullName.trim())
                .email(email)
                .pin(pin)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .emailVerified(true)
                .deleted(false)
                .registeredAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
    }
}
