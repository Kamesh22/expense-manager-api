package com.expensemanager.api.controller;

import com.expensemanager.application.dto.AuthRequestDto;
import com.expensemanager.application.dto.AuthResponseDto;
import com.expensemanager.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication operations.
 * Base path: /api/auth
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     *
     * @param authRequestDto the authentication request with username, email, and password
     * @return authentication response with JWT token and user details
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Create a new user account with username, email, and password"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or username/email already exists"
        )
    })
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody AuthRequestDto authRequestDto) {
        AuthResponseDto response = authService.register(authRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user with username and password.
     *
     * @param authRequestDto the authentication request with username and password
     * @return authentication response with JWT token and user details
     */
    @PostMapping("/login")
    @Operation(
        summary = "Login user",
        description = "Authenticate user with username/email and password, returns JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        )
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto authRequestDto) {
        AuthResponseDto response = authService.login(authRequestDto);
        return ResponseEntity.ok(response);
    }

}
