package com.invoiceme.application.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Customer information")
public record CustomerResponse(
        @Schema(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Customer name", example = "John Doe")
        String name,
        
        @Schema(description = "Customer email", example = "john.doe@example.com")
        String email,
        
        @Schema(description = "Customer address", example = "123 Main St, City, State 12345")
        String address,
        
        @Schema(description = "Customer phone number", example = "+1-555-123-4567")
        String phone,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,
        
        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
}

