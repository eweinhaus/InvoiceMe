package com.invoiceme.presentation.rest;

import com.invoiceme.application.customer.dto.CustomerRequest;
import com.invoiceme.application.customer.dto.CustomerResponse;
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
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    @PostMapping
    @Operation(summary = "Create a new customer")
    @ApiResponse(responseCode = "201", description = "Customer created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<CustomerResponse> create(@RequestBody CustomerRequest request) {
        // Stub implementation - will be implemented in PRD 02
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<CustomerResponse> getById(@PathVariable UUID id) {
        // Stub implementation - will be implemented in PRD 02
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "List all customers")
    @ApiResponse(responseCode = "200", description = "List of customers")
    public ResponseEntity<Page<CustomerResponse>> list(Pageable pageable) {
        // Stub implementation - will be implemented in PRD 02
        return ResponseEntity.ok(Page.empty());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    @ApiResponse(responseCode = "200", description = "Customer updated successfully")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @RequestBody CustomerRequest request) {
        // Stub implementation - will be implemented in PRD 02
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    @ApiResponse(responseCode = "204", description = "Customer deleted successfully")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        // Stub implementation - will be implemented in PRD 02
        return ResponseEntity.noContent().build();
    }
}

