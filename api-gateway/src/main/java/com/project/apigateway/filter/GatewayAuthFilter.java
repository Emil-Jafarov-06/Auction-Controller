package com.project.apigateway.filter;

import com.project.apigateway.model.ValidateTokenRequest;
import com.project.apigateway.model.ValidateTokenResponse;
import com.project.apigateway.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;
    private final AuthorizationService authorizationService;

    @Value("${services.auth.base-url}")
    private String authServiceBaseUrl;

    public GatewayAuthFilter(WebClient.Builder webClientBuilder,
                             AuthorizationService authorizationService) {
        this.webClientBuilder = webClientBuilder;
        this.authorizationService = authorizationService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String accessToken = authHeader.substring(7);

        return webClientBuilder.build()
                .post()
                .uri(authServiceBaseUrl + "/internal/auth/verify-token")
                .bodyValue(new ValidateTokenRequest(accessToken))
                .retrieve()
                .bodyToMono(ValidateTokenResponse.class)
                .flatMap(authResponse -> {
                    if (authResponse.valid() == null || !authResponse.valid()) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    boolean allowed = authorizationService.isAllowed(
                            request.getMethod(),
                            path,
                            authResponse.role()
                    );

                    if (!allowed) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    ServerHttpRequest mutatedRequest = request.mutate()
                            .headers(headers -> {
                                headers.remove("X-User-Id");
                                headers.remove("X-User-Email");
                                headers.remove("X-User-Role");
                            })
                            .header("X-User-Id", String.valueOf(authResponse.id()))
                            .header("X-User-Email", authResponse.email())
                            .header("X-User-Role", authResponse.role())
                            .build();

                    return chain.filter(
                            exchange.mutate()
                                    .request(mutatedRequest)
                                    .build()
                    );
                })
                .onErrorResume(ex -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/api/auth/verify-email")

                || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/auth/v3/api-docs")
                || path.startsWith("/auction/v3/api-docs")
                || path.startsWith("/bidding/v3/api-docs");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}