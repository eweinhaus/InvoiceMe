package com.invoiceme.application.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Invoice information")
public record InvoiceResponse(
        @Schema(description = "Invoice ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID customerId,
        
        @Schema(description = "Customer name", example = "John Doe")
        String customerName,
        
        @Schema(description = "Invoice status", example = "DRAFT")
        String status,
        
        @Schema(description = "Invoice line items")
        List<LineItemResponse> lineItems,
        
        @Schema(description = "Total amount", example = "1000.00")
        BigDecimal totalAmount,
        
        @Schema(description = "Current balance", example = "1000.00")
        BigDecimal balance,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,
        
        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
}
