package com.invoiceme.application.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request to record a payment")
public record PaymentRequest(
        @NotNull(message = "Invoice ID is required")
        @Schema(description = "Invoice ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID invoiceId,
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Schema(description = "Payment amount", example = "500.00")
        BigDecimal amount,
        
        @PastOrPresent(message = "Payment date cannot be in the future")
        @Schema(description = "Payment date", example = "2024-01-15T10:30:00")
        LocalDateTime paymentDate
) {
}

