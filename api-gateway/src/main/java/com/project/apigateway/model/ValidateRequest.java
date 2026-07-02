package com.project.apigateway.model;

public record ValidateRequest(String accessToken, String method, String path) {}
