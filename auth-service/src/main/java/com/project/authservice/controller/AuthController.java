package com.project.authservice.controller;

import com.project.authservice.model.dto.AuthResponse;
import com.project.authservice.model.dto.LoginRequest;
import com.project.authservice.model.dto.RefreshRequest;
import com.project.authservice.model.dto.RegisterRequest;
import com.project.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Authentication", description = "Endpoints for registration, login, token refresh, and email verification")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register new user",
            description = "Creates a new BIDDER user with emailVerified=false and sends/verifies email later."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Email or PIN already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return new ResponseEntity<>("Verify your email to register!", HttpStatus.OK);
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates a verified user and returns access and refresh tokens."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Invalid email/password or unverified email")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "Refresh access token",
            description = "Uses a valid refresh token to generate a new access token and refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh request"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshRequest refreshRequest) {
        AuthResponse authResponse = authService.refreshToken(refreshRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "Verify email",
            description = "Verifies a user's email using the verification token sent to their email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid, expired, or already used token"),
            @ApiResponse(responseCode = "404", description = "User or token not found")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "Email verification token", required = true)
            @RequestParam String token
    ) {
        authService.verifyUser(token);
        return new ResponseEntity<>("Email verified successfully.", HttpStatus.OK);
    }
}