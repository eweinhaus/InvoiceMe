package com.invoiceme.application.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create or update a customer")
public record CustomerRequest(
        @NotBlank(message = "Name is required")
        @Schema(description = "Customer name", example = "John Doe")
        String name,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(description = "Customer email", example = "john.doe@example.com")
        String email,
        
        @Schema(description = "Customer address", example = "123 Main St, City, State 12345")
        String address,
        
        @Schema(description = "Customer phone number", example = "+1-555-123-4567")
        String phone
) {
}

