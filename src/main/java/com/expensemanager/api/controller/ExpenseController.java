package com.expensemanager.api.controller;

import com.expensemanager.application.dto.ExpenseRequestDto;
import com.expensemanager.application.dto.ExpenseResponseDto;
import com.expensemanager.application.service.ExpenseService;
import com.expensemanager.domain.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Expense operations.
 * Base path: /api/v1/expenses
 */
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Management", description = "Endpoints for managing expenses")
@Slf4j
public class ExpenseController extends BaseController {

    private final ExpenseService expenseService;

    /**
     * Get all expenses for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return paginated list of expenses
     */
    @GetMapping
    @Operation(summary = "Get user expenses", description = "Retrieve all expenses for a user with pagination")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<Page<ExpenseResponseDto>> getAllExpenses(
            @Parameter(description = "User ID") @RequestParam Long userId,
            Pageable pageable) {
        log.debug("Fetching expenses for user: {} with pagination", userId);
        Page<ExpenseResponseDto> expenses = expenseService.getExpensesByUserId(userId, pageable);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Get expense by ID.
     *
     * @param id the expense ID
     * @return expense details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID", description = "Retrieve a specific expense")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expense retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ExpenseResponseDto> getExpenseById(
            @Parameter(description = "Expense ID") @PathVariable Long id) {
        log.debug("Fetching expense with ID: {}", id);
        ExpenseResponseDto expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    /**
     * Get expenses filtered by category.
     *
     * @param userId the user ID
     * @param category the expense category
     * @param pageable pagination information
     * @return paginated list of expenses
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get expenses by category", description = "Retrieve expenses filtered by category")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<Page<ExpenseResponseDto>> getExpensesByCategory(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Expense Category") @PathVariable ExpenseCategory category,
            Pageable pageable) {
        log.debug("Fetching expenses for user: {} with category: {}", userId, category);
        Page<ExpenseResponseDto> expenses = expenseService.getExpensesByCategory(userId, category, pageable);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Get expenses within a date range.
     *
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of expenses in date range
     */
    @GetMapping("/range")
    @Operation(summary = "Get expenses by date range", description = "Retrieve expenses within a date range")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByDateRange(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Start Date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End Date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Fetching expenses for user: {} between {} and {}", userId, startDate, endDate);
        List<ExpenseResponseDto> expenses = expenseService.getExpensesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Create a new expense.
     *
     * @param userId the user ID
     * @param expenseRequestDto expense details
     * @return created expense
     */
    @PostMapping
    @Operation(summary = "Create new expense", description = "Create a new expense record")
    @ApiResponse(responseCode = "201", description = "Expense created successfully")
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Valid @RequestBody ExpenseRequestDto expenseRequestDto) {
        log.info("Creating new expense for user: {}", userId);
        ExpenseResponseDto expense = expenseService.createExpense(userId, expenseRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }

    /**
     * Update expense information.
     *
     * @param id the expense ID
     * @param expenseRequestDto updated expense details
     * @return updated expense
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update expense", description = "Update an existing expense")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @Parameter(description = "Expense ID") @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestDto expenseRequestDto) {
        log.info("Updating expense with ID: {}", id);
        ExpenseResponseDto expense = expenseService.updateExpense(id, expenseRequestDto);
        return ResponseEntity.ok(expense);
    }

    /**
     * Delete expense by ID.
     *
     * @param id the expense ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense", description = "Delete an expense record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "Expense ID") @PathVariable Long id) {
        log.info("Deleting expense with ID: {}", id);
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

}
