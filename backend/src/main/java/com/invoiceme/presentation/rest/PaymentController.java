package com.invoiceme.presentation.rest;

import com.invoiceme.application.payment.dto.PaymentRequest;
import com.invoiceme.application.payment.dto.PaymentResponse;
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
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    @PostMapping
    @Operation(summary = "Record a payment")
    @ApiResponse(responseCode = "201", description = "Payment recorded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "422", description = "Payment amount exceeds invoice balance")
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        // Stub implementation - will be implemented in PRD 06
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        // Stub implementation - will be implemented in PRD 06
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "List payments with optional invoice filter")
    @ApiResponse(responseCode = "200", description = "List of payments")
    public ResponseEntity<Page<PaymentResponse>> list(
            @RequestParam(required = false) UUID invoiceId,
            Pageable pageable) {
        // Stub implementation - will be implemented in PRD 06
        return ResponseEntity.ok(Page.empty());
    }
}

