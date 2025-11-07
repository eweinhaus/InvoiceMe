package com.invoiceme.application.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create or update an invoice")
public record InvoiceRequest(
        @NotNull(message = "Customer ID is required")
        @Schema(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID customerId,
        
        @NotEmpty(message = "At least one line item is required")
        @Valid
        @Schema(description = "Invoice line items")
        List<LineItemRequest> lineItems
) {
    @Schema(description = "Invoice line item")
    public record LineItemRequest(
            @Schema(description = "Item description", example = "Web Development Services")
            String description,
            
            @Schema(description = "Quantity", example = "10")
            Integer quantity,
            
            @Schema(description = "Unit price", example = "100.00")
            BigDecimal unitPrice
    ) {
    }
}

