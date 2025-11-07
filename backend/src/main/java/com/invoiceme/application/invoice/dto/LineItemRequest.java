package com.invoiceme.application.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request for a line item")
public record LineItemRequest(
        @NotBlank(message = "Description is required")
        @Schema(description = "Line item description", example = "Web Development Services")
        String description,
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Quantity", example = "10")
        Integer quantity,
        
        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", message = "Unit price must be greater than or equal to 0")
        @Schema(description = "Unit price", example = "100.00")
        BigDecimal unitPrice
) {
}

