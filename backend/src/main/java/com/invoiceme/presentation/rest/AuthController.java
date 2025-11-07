package com.invoiceme.presentation.rest;

import com.invoiceme.application.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    @GetMapping("/user")
    @Operation(summary = "Get current authenticated user")
    @ApiResponse(responseCode = "200", description = "User information")
    @ApiResponse(responseCode = "401", description = "User not authenticated")
    public ResponseEntity<UserResponse> getCurrentUser() {
        // Stub implementation - will be implemented in PRD 08
        return ResponseEntity.ok().build();
    }
}

