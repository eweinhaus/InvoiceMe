package com.invoiceme.presentation.rest;

import com.invoiceme.application.payment.PaymentCommandService;
import com.invoiceme.application.payment.PaymentQueryService;
import com.invoiceme.application.payment.dto.PaymentRequest;
import com.invoiceme.application.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment management endpoints")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    @PostMapping
    @Operation(summary = "Record a payment", description = "Records a payment for an invoice and updates invoice balance")
    @ApiResponse(responseCode = "201", description = "Payment recorded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Invoice not found")
    @ApiResponse(responseCode = "422", description = "Payment amount exceeds invoice balance")
    public ResponseEntity<PaymentResponse> recordPayment(@RequestBody @Valid PaymentRequest request) {
        PaymentResponse response = paymentCommandService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        PaymentResponse response = paymentQueryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List payments with optional filter", 
               description = "Lists payments with pagination. Can filter by invoiceId")
    @ApiResponse(responseCode = "200", description = "List of payments")
    public ResponseEntity<Page<PaymentResponse>> list(
            @RequestParam(required = false) UUID invoiceId,
            @PageableDefault(size = 20, sort = "paymentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<PaymentResponse> response;
        
        if (invoiceId != null) {
            response = paymentQueryService.getByInvoiceId(invoiceId, pageable);
        } else {
            response = paymentQueryService.getAll(pageable);
        }
        
        return ResponseEntity.ok(response);
    }
}
