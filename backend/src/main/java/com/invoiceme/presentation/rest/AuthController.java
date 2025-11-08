package com.invoiceme.presentation.rest;

import com.invoiceme.application.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        
        if (oauth2User == null) {
            return ResponseEntity.status(401).build();
        }

        // Extract user information from OAuth2User
        String id = oauth2User.getAttribute("sub");
        if (id == null) {
            // Fallback to name if sub is not available
            id = oauth2User.getName();
        }
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        UserResponse userResponse = new UserResponse(
                id != null ? id : "",
                email != null ? email : "",
                name != null ? name : "",
                picture != null ? picture : ""
        );

        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    @ApiResponse(responseCode = "200", description = "Successfully logged out")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Get the current authentication
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Perform logout - this clears the security context and invalidates the session
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        return ResponseEntity.ok().build();
    }
}

