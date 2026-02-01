package com.expensemanager.api.controller;

import com.expensemanager.api.exception.ValidationException;
import com.expensemanager.application.dto.RoleChangeRequestDto;
import com.expensemanager.application.dto.StatusChangeRequestDto;
import com.expensemanager.application.dto.UserResponseDto;
import com.expensemanager.application.service.UserService;
import com.expensemanager.domain.entity.User;
import com.expensemanager.domain.enums.Role;
import com.expensemanager.infrastructure.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Admin API endpoints for user management.
 * All endpoints require ADMIN role and are protected by method-level security.
 * 
 * Base path: /api/v1/admin (context path /api/v1 configured in application.yml)
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Management", description = "Admin-only endpoints for user management")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController extends BaseController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Get all users with pagination.
     * AUTHORIZATION: Requires ADMIN role
     *
     * @param pageable pagination parameters
     * @return paginated list of users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all users",
        description = "Retrieve all users in the system with pagination. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        log.debug("ADMIN: Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    /**
     * Get a specific user by ID.
     * AUTHORIZATION: Requires ADMIN role
     *
     * @param userId the user ID to retrieve
     * @return the user details
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieve a specific user by their ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
        log.debug("ADMIN: Fetching user with ID: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Change a user's role.
     * AUTHORIZATION: Requires ADMIN role
     * SECURITY: Prevents ADMIN from demoting themselves to non-ADMIN roles
     *
     * @param userId the user ID whose role to change
     * @param roleChangeDto the new role
     * @return the updated user
     */
    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Change user role",
        description = "Change a user's role. ADMIN users cannot demote themselves. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User role changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role change (e.g., ADMIN cannot demote themselves)"),
        @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    public ResponseEntity<UserResponseDto> changeUserRole(
        @PathVariable Long userId,
        @Valid @RequestBody RoleChangeRequestDto roleChangeDto) {

        log.info("ADMIN: Attempting to change role for user ID: {} to role: {}", userId, roleChangeDto.getRole());

        // Get authenticated admin user ID
        Long adminUserId = getAuthenticatedUserId();

        // Prevent ADMIN from demoting themselves to non-ADMIN role
        if (userId.equals(adminUserId) && !roleChangeDto.getRole().equals(Role.ADMIN)) {
            log.warn("SECURITY: ADMIN user {} attempted to demote themselves to role: {}", adminUserId, roleChangeDto.getRole());
            throw new ValidationException("ADMIN users cannot demote themselves to a lower role");
        }

        // Fetch the user to update
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new com.expensemanager.api.exception.ResourceNotFoundException(
                "User not found with ID: " + userId));

        // Update role
        user.setRole(roleChangeDto.getRole());
        userRepository.save(user);

        log.info("ADMIN: User {} role changed to: {}", userId, roleChangeDto.getRole());
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Change a user's active status.
     * AUTHORIZATION: Requires ADMIN role
     * SECURITY: Prevents ADMIN from deactivating themselves
     *
     * @param userId the user ID whose status to change
     * @param statusChangeDto the new active status
     * @return the updated user
     */
    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Change user status",
        description = "Activate or deactivate a user. ADMIN users cannot deactivate themselves. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status change (e.g., ADMIN cannot deactivate themselves)"),
        @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    public ResponseEntity<UserResponseDto> changeUserStatus(
        @PathVariable Long userId,
        @Valid @RequestBody StatusChangeRequestDto statusChangeDto) {

        log.info("ADMIN: Attempting to change status for user ID: {} to status: {}", userId, statusChangeDto.getIsActive());

        // Get authenticated admin user ID
        Long adminUserId = getAuthenticatedUserId();

        // Prevent ADMIN from deactivating themselves
        if (userId.equals(adminUserId) && !statusChangeDto.getIsActive()) {
            log.warn("SECURITY: ADMIN user {} attempted to deactivate themselves", adminUserId);
            throw new ValidationException("ADMIN users cannot deactivate themselves");
        }

        // Fetch the user to update
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new com.expensemanager.api.exception.ResourceNotFoundException(
                "User not found with ID: " + userId));

        // Update status
        user.setIsActive(statusChangeDto.getIsActive());
        userRepository.save(user);

        log.info("ADMIN: User {} status changed to: {}", userId, statusChangeDto.getIsActive());
        return ResponseEntity.ok(userService.getUserById(userId));
    }

}
