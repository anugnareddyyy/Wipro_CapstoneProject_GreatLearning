package com.appverse.controller;

import com.appverse.dto.request.LoginRequest;
import com.appverse.dto.request.RegisterRequest;
import com.appverse.dto.response.AuthResponse;
import com.appverse.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 *
 * All endpoints are publicly accessible (no JWT required).
 * Validated request bodies are handled by Spring's @Valid annotation.
 * Validation errors propagate to GlobalExceptionHandler for consistent formatting.
 *
 * Base URL: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Register, login, and token management")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     *
     * POST /api/auth/register
     *
     * Roles: PUBLIC
     * Validations: email uniqueness, username uniqueness, password strength
     *
     * @param request validated registration form data
     * @return 201 Created with JWT token and user profile
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user and return a JWT token.
     *
     * POST /api/auth/login
     *
     * Roles: PUBLIC
     *
     * @param request login credentials (email + password)
     * @return 200 OK with JWT token and user profile
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate and get JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
