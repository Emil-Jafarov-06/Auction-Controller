package com.project.msbidding.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI auctionControllerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auction Controller API")
                        .description("API documentation for Auction, Bid, and Bidder operations.")
                        .version("1.0.0")
                        .contact(new Contact().email("EmilJafarov3841@gmail.com")
                                .name("Emil Jafarov")));
    }

}
