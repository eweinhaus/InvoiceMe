package com.invoiceme.application.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response for a line item")
public record LineItemResponse(
        @Schema(description = "Item description", example = "Web Development Services")
        String description,
        
        @Schema(description = "Quantity", example = "10")
        Integer quantity,
        
        @Schema(description = "Unit price", example = "100.00")
        BigDecimal unitPrice,
        
        @Schema(description = "Subtotal (quantity * unit price)", example = "1000.00")
        BigDecimal subtotal
) {
}

