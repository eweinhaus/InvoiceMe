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
        
        @Schema(description = "Invoice status", example = "DRAFT")
        String status,
        
        @Schema(description = "Total amount", example = "1000.00")
        BigDecimal totalAmount,
        
        @Schema(description = "Current balance", example = "1000.00")
        BigDecimal balance,
        
        @Schema(description = "Line items")
        List<LineItemResponse> lineItems,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,
        
        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
    @Schema(description = "Invoice line item")
    public record LineItemResponse(
            @Schema(description = "Item description", example = "Web Development Services")
            String description,
            
            @Schema(description = "Quantity", example = "10")
            Integer quantity,
            
            @Schema(description = "Unit price", example = "100.00")
            BigDecimal unitPrice,
            
            @Schema(description = "Subtotal", example = "1000.00")
            BigDecimal subtotal
    ) {
    }
}

