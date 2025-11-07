package com.invoiceme.application.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Payment information")
public record PaymentResponse(
        @Schema(description = "Payment ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Invoice ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID invoiceId,
        
        @Schema(description = "Payment amount", example = "500.00")
        BigDecimal amount,
        
        @Schema(description = "Payment date", example = "2024-01-15T10:30:00")
        LocalDateTime paymentDate,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {
}

