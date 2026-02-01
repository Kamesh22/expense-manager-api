package com.expensemanager.api.controller;

import com.expensemanager.application.dto.ExpenseRequestDto;
import com.expensemanager.application.dto.ExpenseResponseDto;
import com.expensemanager.application.service.ExpenseService;
import com.expensemanager.domain.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Expense operations with strict ownership enforcement.
 * 
 * SECURITY NOTES:
 * - User ID is ALWAYS extracted from SecurityContext (JWT token)
 * - No userId parameters are accepted from clients
 * - Users can ONLY create, read, update, or delete their own expenses
 * - Ownership violations result in HTTP 403 (Forbidden)
 * 
 * Base path: /api/v1/expenses
 */
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Management", description = "Endpoints for managing expenses")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class ExpenseController extends BaseController {

    private final ExpenseService expenseService;

    /**
     * Get all expenses for the authenticated user with pagination.
     * 
     * User ID is extracted from JWT token - NOT from request parameters.
     *
     * @param pageable pagination information
     * @param authentication Spring Security authentication object
     * @return paginated list of authenticated user's expenses
     */
    @GetMapping
    @Operation(
        summary = "Get authenticated user's expenses",
        description = "Retrieve all expenses for the authenticated user with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<Page<ExpenseResponseDto>> getUserExpenses(
            Pageable pageable,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("Fetching expenses for authenticated user: {}", userId);
        Page<ExpenseResponseDto> expenses = expenseService.getExpensesByUserId(userId, pageable);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Get a specific expense by ID with ownership verification.
     * 
     * Returns 403 Forbidden if the expense does not belong to the authenticated user.
     *
     * @param expenseId the expense ID
     * @param authentication Spring Security authentication object
     * @return the expense details
     */
    @GetMapping("/{expenseId}")
    @Operation(
        summary = "Get expense by ID",
        description = "Retrieve a specific expense with ownership verification"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expense retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - expense belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ExpenseResponseDto> getExpenseById(
            @Parameter(description = "Expense ID") @PathVariable Long expenseId,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("Fetching expense with ID: {} for user: {}", expenseId, userId);
        ExpenseResponseDto expense = expenseService.getExpenseById(expenseId, userId);
        return ResponseEntity.ok(expense);
    }

    /**
     * Get expenses filtered by category for the authenticated user.
     * 
     * User ID is extracted from JWT token - NOT from request parameters.
     *
     * @param category the expense category
     * @param pageable pagination information
     * @param authentication Spring Security authentication object
     * @return paginated list of expenses in the specified category
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get expenses by category",
        description = "Retrieve expenses filtered by category for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<Page<ExpenseResponseDto>> getExpensesByCategory(
            @Parameter(description = "Expense Category") @PathVariable ExpenseCategory category,
            Pageable pageable,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("Fetching {} expenses for user: {}", category, userId);
        Page<ExpenseResponseDto> expenses = expenseService.getExpensesByCategory(userId, category, pageable);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Get expenses within a date range for the authenticated user.
     * 
     * User ID is extracted from JWT token - NOT from request parameters.
     *
     * @param startDate the start date (inclusive) in YYYY-MM-DD format
     * @param endDate the end date (inclusive) in YYYY-MM-DD format
     * @param authentication Spring Security authentication object
     * @return list of expenses within the date range
     */
    @GetMapping("/range")
    @Operation(
        summary = "Get expenses by date range",
        description = "Retrieve expenses within a date range for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByDateRange(
            @Parameter(description = "Start Date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End Date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("Fetching expenses for user {} between {} and {}", userId, startDate, endDate);
        List<ExpenseResponseDto> expenses = expenseService.getExpensesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Create a new expense for the authenticated user.
     * 
     * User ID is ALWAYS extracted from JWT token - NOT from request parameters.
     * This prevents users from creating expenses for other users.
     *
     * @param expenseRequestDto expense details (does NOT include userId)
     * @param authentication Spring Security authentication object
     * @return the created expense
     */
    @PostMapping
    @Operation(
        summary = "Create new expense",
        description = "Create a new expense for the authenticated user"
    )
    @ApiResponse(responseCode = "201", description = "Expense created successfully")
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @Valid @RequestBody ExpenseRequestDto expenseRequestDto,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("Creating new expense for authenticated user: {}", userId);
        ExpenseResponseDto expense = expenseService.createExpense(userId, expenseRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    /**
     * Update an expense with ownership verification.
     * 
     * Returns 403 Forbidden if the expense does not belong to the authenticated user.
     *
     * @param expenseId the expense ID
     * @param expenseRequestDto updated expense details
     * @param authentication Spring Security authentication object
     * @return the updated expense
     */
    @PutMapping("/{expenseId}")
    @Operation(
        summary = "Update expense",
        description = "Update an existing expense with ownership verification"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - expense belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @Parameter(description = "Expense ID") @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseRequestDto expenseRequestDto,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("Updating expense with ID: {} for user: {}", expenseId, userId);
        ExpenseResponseDto expense = expenseService.updateExpense(expenseId, userId, expenseRequestDto);
        return ResponseEntity.ok(expense);
    }

    /**
     * Delete an expense with ownership verification.
     * 
     * Returns 403 Forbidden if the expense does not belong to the authenticated user.
     *
     * @param expenseId the expense ID
     * @param authentication Spring Security authentication object
     * @return no content
     */
    @DeleteMapping("/{expenseId}")
    @Operation(
        summary = "Delete expense",
        description = "Delete an expense with ownership verification"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - expense belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "Expense ID") @PathVariable Long expenseId,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        log.info("Deleting expense with ID: {} for user: {}", expenseId, userId);
        expenseService.deleteExpense(expenseId, userId);
        return ResponseEntity.noContent().build();
    }

}
