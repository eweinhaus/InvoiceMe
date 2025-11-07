package com.invoiceme.application.invoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request to update an invoice")
public record UpdateInvoiceRequest(
        @NotEmpty(message = "At least one line item is required")
        @Valid
        @Schema(description = "Invoice line items")
        List<LineItemRequest> lineItems
) {
}

