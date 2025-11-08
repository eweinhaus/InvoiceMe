package com.invoiceme;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InvoiceMeApplication {

    public static void main(String[] args) {
        // Load .env file before Spring Boot starts
        // This makes environment variables available to application.yml
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
        
        // Set system properties from .env file so Spring Boot can read them
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        
        SpringApplication.run(InvoiceMeApplication.class, args);
    }
}

