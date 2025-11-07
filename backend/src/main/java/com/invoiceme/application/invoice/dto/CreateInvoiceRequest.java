package com.invoiceme.application.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create an invoice")
public record CreateInvoiceRequest(
        @NotNull(message = "Customer ID is required")
        @Schema(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID customerId,
        
        @NotEmpty(message = "At least one line item is required")
        @Valid
        @Schema(description = "Invoice line items")
        List<LineItemRequest> lineItems
) {
}

