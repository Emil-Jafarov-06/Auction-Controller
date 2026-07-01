package com.project.apigateway.model;

public record ValidateTokenResponse(Boolean valid, Long id, String email, String role) {}
