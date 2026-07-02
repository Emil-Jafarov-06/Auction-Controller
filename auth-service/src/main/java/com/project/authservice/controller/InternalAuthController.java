package com.project.authservice.controller;

import com.project.authservice.model.dto.ValidateRequest;
import com.project.authservice.model.dto.ValidateResponse;
import com.project.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final AuthService authService;

    @PostMapping("/verify-token")
    public ResponseEntity<ValidateResponse> verifyToken(@RequestBody @Valid ValidateRequest validateRequest){
        return new ResponseEntity<>(authService.validateToken(validateRequest), HttpStatus.OK);
    }

}
