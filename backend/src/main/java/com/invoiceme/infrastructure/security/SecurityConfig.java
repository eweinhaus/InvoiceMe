package com.invoiceme.infrastructure.security;

import com.invoiceme.infrastructure.config.CorsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfig corsConfig;

    @Value("${app.auth.dev-mode:false}")
    private boolean devMode;

    public SecurityConfig(CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Enable OAuth2 login when dev mode is disabled
        if (!devMode) {
            http
                    .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                    .csrf(csrf -> csrf.disable()) // Will be enabled in PRD 08 with proper OAuth setup
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/api/**").permitAll() // Permissive for now, will be secured in PRD 08
                            .anyRequest().authenticated()
                    )
                    .oauth2Login(oauth2 -> oauth2
                            .defaultSuccessUrl("http://localhost:5173/customers", true)
                            .failureUrl("http://localhost:5173/login?error=true")
                    )
                    .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // For H2 console
        } else {
            http
                    .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/api/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        }

        return http.build();
    }
}

