package com.invoiceme.presentation.rest;

import com.invoiceme.application.customer.CustomerCommandService;
import com.invoiceme.application.customer.CustomerQueryService;
import com.invoiceme.application.customer.dto.CustomerRequest;
import com.invoiceme.application.customer.dto.CustomerResponse;
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
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerCommandService commandService;
    private final CustomerQueryService queryService;

    @PostMapping
    @Operation(summary = "Create a new customer")
    @ApiResponse(responseCode = "201", description = "Customer created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "422", description = "Duplicate email")
    public ResponseEntity<CustomerResponse> create(@RequestBody @Valid CustomerRequest request) {
        CustomerResponse response = commandService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<CustomerResponse> getById(@PathVariable UUID id) {
        CustomerResponse response = queryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all customers")
    @ApiResponse(responseCode = "200", description = "List of customers")
    public ResponseEntity<Page<CustomerResponse>> list(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CustomerResponse> response = queryService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    @ApiResponse(responseCode = "200", description = "Customer updated successfully")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "422", description = "Duplicate email")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid CustomerRequest request) {
        CustomerResponse response = commandService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    @ApiResponse(responseCode = "204", description = "Customer deleted successfully")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        commandService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}

