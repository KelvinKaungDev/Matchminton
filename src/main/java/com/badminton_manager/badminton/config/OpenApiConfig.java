package com.badminton_manager.badminton.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Badminton Manager API")
                        .description("REST API for managing badminton groups, competitions, courts, and games")
                        .version("1.0.0"));
    }
}
