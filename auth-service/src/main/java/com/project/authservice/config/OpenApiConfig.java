package com.project.authservice.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static String SECURITY_SCHEME_NAME = "AUTH_SECURITY_SCHEME";

    @Bean
    public OpenAPI auctionControllerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auction Controller API")
                        .description("API documentation for Auction, Bid, and Bidder operations.")
                        .version("1.0.0")
                        .contact(new Contact().email("EmilJafarov3841@gmail.com")
                                .name("Emil Jafarov")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME)
                )
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }

}
