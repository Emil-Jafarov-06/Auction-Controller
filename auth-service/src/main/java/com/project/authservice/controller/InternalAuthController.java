package com.project.authservice.controller;

import com.project.authservice.model.dto.ValidateTokenRequest;
import com.project.authservice.model.dto.ValidateTokenResponse;
import com.project.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final AuthService authService;

    @GetMapping("/verify-token")
    public ResponseEntity<ValidateTokenResponse> verifyToken(@RequestBody @Valid ValidateTokenRequest validateTokenRequest){
        return new ResponseEntity<>(authService.validateToken(validateTokenRequest), HttpStatus.OK);
    }

}
