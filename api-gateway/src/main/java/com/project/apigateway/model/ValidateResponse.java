package com.project.apigateway.model;

public record ValidateResponse(Boolean authenticated, Boolean authorized, Long id, String email, String role) {}
