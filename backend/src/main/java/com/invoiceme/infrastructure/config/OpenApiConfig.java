package com.invoiceme.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI invoiceMeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("InvoiceMe API")
                        .version("1.0.0")
                        .description("InvoiceMe REST API - Invoice Management System"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")
                ));
    }
}

