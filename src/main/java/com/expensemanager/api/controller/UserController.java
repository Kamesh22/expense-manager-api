package com.expensemanager.api.controller;

import com.expensemanager.application.dto.UserRequestDto;
import com.expensemanager.application.dto.UserResponseDto;
import com.expensemanager.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User operations.
 * Base path: /api/v1/users
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users")
@Slf4j
public class UserController extends BaseController {

    private final UserService userService;

    /**
     * Get all users with pagination.
     *
     * @param pageable pagination information
     * @return paginated list of users
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID.
     *
     * @param id the user ID
     * @return user details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.debug("Fetching user with ID: {}", id);
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Create a new user.
     *
     * @param userRequestDto user details
     * @return created user
     */
    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user account")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserRequestDto userRequestDto) {
        log.info("Creating new user with username: {}", userRequestDto.getUsername());
        UserResponseDto user = userService.createUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Update user information.
     *
     * @param id the user ID
     * @param userRequestDto updated user details
     * @return updated user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserRequestDto userRequestDto) {
        log.info("Updating user with ID: {}", id);
        UserResponseDto user = userService.updateUser(id, userRequestDto);
        return ResponseEntity.ok(user);
    }

    /**
     * Delete user by ID.
     *
     * @param id the user ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
