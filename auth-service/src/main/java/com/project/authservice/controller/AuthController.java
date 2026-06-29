package com.project.authservice.controller;

import com.project.authservice.model.dto.AuthResponse;
import com.project.authservice.model.dto.LoginRequest;
import com.project.authservice.model.dto.RefreshRequest;
import com.project.authservice.model.dto.RegisterRequest;
import com.project.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return new ResponseEntity<>("Verify your email to register!", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshRequest refreshRequest) {
        AuthResponse authResponse = authService.refreshToken(refreshRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyUser(token);
        return new ResponseEntity<>("Registration finished! New user created!", HttpStatus.CREATED);
    }

}
