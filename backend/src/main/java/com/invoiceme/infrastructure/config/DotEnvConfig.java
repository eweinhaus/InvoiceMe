package com.invoiceme.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotEnvConfig {

    @Bean
    public Dotenv dotenv() {
        // Load .env file from the project root (backend directory)
        return Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
    }
}

