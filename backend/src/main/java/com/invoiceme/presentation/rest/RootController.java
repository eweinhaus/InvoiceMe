package com.invoiceme.presentation.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Root", description = "Root endpoint")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "API root endpoint")
    @ApiResponse(responseCode = "200", description = "API information")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "name", "InvoiceMe API",
                "version", "1.0.0",
                "status", "running",
                "docs", "/swagger-ui.html",
                "api", "/api"
        ));
    }
}

