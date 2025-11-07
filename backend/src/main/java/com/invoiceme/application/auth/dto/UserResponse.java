package com.invoiceme.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current authenticated user information")
public record UserResponse(
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,
        
        @Schema(description = "User email", example = "user@example.com")
        String email,
        
        @Schema(description = "User name", example = "John Doe")
        String name,
        
        @Schema(description = "User picture URL", example = "https://example.com/avatar.jpg")
        String picture
) {
}

