package com.invoiceme.presentation.rest;

import com.invoiceme.application.invoice.dto.InvoiceRequest;
import com.invoiceme.application.invoice.dto.InvoiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {

    @PostMapping
    @Operation(summary = "Create a new invoice")
    @ApiResponse(responseCode = "201", description = "Invoice created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<InvoiceResponse> create(@RequestBody InvoiceRequest request) {
        // Stub implementation - will be implemented in PRD 04
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    @ApiResponse(responseCode = "200", description = "Invoice found")
    @ApiResponse(responseCode = "404", description = "Invoice not found")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable UUID id) {
        // Stub implementation - will be implemented in PRD 04
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "List invoices with optional filters")
    @ApiResponse(responseCode = "200", description = "List of invoices")
    public ResponseEntity<Page<InvoiceResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID customerId,
            Pageable pageable) {
        // Stub implementation - will be implemented in PRD 04
        return ResponseEntity.ok(Page.empty());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update invoice (only if DRAFT)")
    @ApiResponse(responseCode = "200", description = "Invoice updated successfully")
    @ApiResponse(responseCode = "404", description = "Invoice not found")
    @ApiResponse(responseCode = "422", description = "Invoice cannot be updated (not in DRAFT status)")
    public ResponseEntity<InvoiceResponse> update(@PathVariable UUID id, @RequestBody InvoiceRequest request) {
        // Stub implementation - will be implemented in PRD 04
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Mark invoice as SENT")
    @ApiResponse(responseCode = "200", description = "Invoice marked as SENT")
    @ApiResponse(responseCode = "404", description = "Invoice not found")
    @ApiResponse(responseCode = "422", description = "Invoice cannot be sent (invalid status)")
    public ResponseEntity<InvoiceResponse> markAsSent(@PathVariable UUID id) {
        // Stub implementation - will be implemented in PRD 04
        return ResponseEntity.ok().build();
    }
}

