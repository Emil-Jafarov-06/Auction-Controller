package com.project.apigateway.service;

import com.project.apigateway.config.AuthorizationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

@Service
public class AuthorizationService {

    private final AuthorizationProperties authorizationProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthorizationService(AuthorizationProperties authorizationProperties) {
        this.authorizationProperties = authorizationProperties;
    }

    public boolean isAllowed(HttpMethod method, String path, String role) {
        return authorizationProperties.getRules()
                .stream()
                .anyMatch(rule ->
                        rule.getMethod().equalsIgnoreCase(method.name())
                                && pathMatcher.match(rule.getPath(), path)
                                && rule.getRoles().contains(role)
                );
    }
}
