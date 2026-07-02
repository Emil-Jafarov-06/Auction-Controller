package com.project.authservice.service;

import com.project.authservice.config.AuthorizationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

@Service
public class AuthorizationService {

    private final AuthorizationProperties authorizationProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthorizationService(AuthorizationProperties authorizationProperties) {
        this.authorizationProperties = authorizationProperties;
    }

    public boolean isAllowed(String method, String path, String role) {
        return authorizationProperties.getRules()
                .stream()
                .anyMatch(rule ->
                        rule.getMethod().equalsIgnoreCase(method)
                                && pathMatcher.match(rule.getPath(), path)
                                && rule.getRoles().contains(role)
                );
    }
}
