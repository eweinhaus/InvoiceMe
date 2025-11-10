package com.invoiceme.presentation.rest;

import com.invoiceme.application.invoice.InvoiceCommandService;
import com.invoiceme.application.invoice.InvoiceQueryService;
import com.invoiceme.application.invoice.dto.CreateInvoiceRequest;
import com.invoiceme.application.invoice.dto.InvoiceResponse;
import com.invoiceme.application.invoice.dto.UpdateInvoiceRequest;
import com.invoiceme.domain.invoice.InvoiceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Invoice management endpoints")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceCommandService invoiceCommandService;
    private final InvoiceQueryService invoiceQueryService;
    private final com.invoiceme.application.invoice.InvoicePdfService invoicePdfService;

    @PostMapping
    @Operation(summary = "Create a new invoice", description = "Creates a new invoice in DRAFT status with line items")
    @ApiResponse(responseCode = "201", description = "Invoice created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<InvoiceResponse> create(@RequestBody @Valid CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceCommandService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    @ApiResponse(responseCode = "200", description = "Invoice found")
    @ApiResponse(responseCode = "404", description = "Invoice not found")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable UUID id) {
        InvoiceResponse response = invoiceQueryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List invoices with optional filters", 
               description = "Lists invoices with pagination. Can filter by status and/or customerId")
    @ApiResponse(responseCode = "200", description = "List of invoices")
    public ResponseEntity<Page<InvoiceResponse>> list(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) UUID customerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<InvoiceResponse> response;
        
        if (status != null && customerId != null) {
            // Filter by both status and customer - get by customer first, then filter by status
            // (Note: For better performance, could add custom repository method findByCustomerIdAndStatus)
            Page<InvoiceResponse> customerInvoices = invoiceQueryService.getByCustomerId(customerId, pageable);
            List<InvoiceResponse> filtered = customerInvoices.getContent().stream()
                    .filter(invoice -> invoice.status().equals(status.name()))
                    .collect(Collectors.toList());
            response = new PageImpl<>(
                    filtered, 
                    customerInvoices.getPageable(), 
                    filtered.size()
            );
        } else if (status != null) {
            response = invoiceQueryService.getByStatus(status, pageable);
        } else if (customerId != null) {
            response = invoiceQueryService.getByCustomerId(customerId, pageable);
        } else {
            response = invoiceQueryService.getAll(pageable);
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update invoice (only if DRAFT)", 
               description = "Updates invoice line items. Only allowed if invoice is in DRAFT status")
    @ApiResponse(responseCode = "200", description = "Invoice updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Invoice not found")
    @ApiResponse(responseCode = "422", description = "Invoice cannot be updated (not in DRAFT status)")
    public ResponseEntity<InvoiceResponse> update(
            @PathVariable UUID id, 
            @RequestBody @Valid UpdateInvoiceRequest request) {
        InvoiceResponse response = invoiceCommandService.updateInvoice(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Mark invoice as SENT", 
               description = "Transitions invoice from DRAFT to SENT status. Validates invoice has line items and total > 0")
    @ApiResponse(responseCode = "200", description = "Invoice marked as SENT")
    @ApiResponse(responseCode = "404", description = "Invoice not found")
    @ApiResponse(responseCode = "422", description = "Invoice cannot be sent (invalid status or missing requirements)")
    public ResponseEntity<InvoiceResponse> markAsSent(@PathVariable UUID id) {
        InvoiceResponse response = invoiceCommandService.markAsSent(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download invoice as PDF", 
               description = "Generates and downloads the invoice as a PDF document")
    @ApiResponse(responseCode = "200", description = "PDF generated successfully")
    @ApiResponse(responseCode = "404", description = "Invoice not found")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        byte[] pdfBytes = invoicePdfService.generatePdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + id.toString().substring(0, 8) + ".pdf");
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}

