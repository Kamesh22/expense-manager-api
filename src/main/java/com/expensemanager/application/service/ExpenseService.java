package com.expensemanager.application.service;

import com.expensemanager.application.dto.ExpenseRequestDto;
import com.expensemanager.application.dto.ExpenseResponseDto;
import com.expensemanager.domain.enums.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Expense operations.
 * 
 * All operations are scoped to the authenticated user's ID extracted from SecurityContext.
 * Strict ownership enforcement: users can only access their own expenses.
 */
public interface ExpenseService {

    /**
     * Create a new expense for the authenticated user.
     * 
     * The user ID is extracted from SecurityContext, not from request parameters.
     * This ensures users cannot create expenses for other users.
     *
     * @param userId the authenticated user ID (from SecurityContext)
     * @param expenseRequestDto the expense request DTO
     * @return the created expense as response DTO
     * @throws ResourceNotFoundException if user not found
     * @throws ValidationException if expense data is invalid
     */
    ExpenseResponseDto createExpense(Long userId, ExpenseRequestDto expenseRequestDto);

    /**
     * Get expense by ID with ownership verification.
     * 
     * Throws AccessDeniedException if the expense does not belong to the authenticated user.
     *
     * @param expenseId the expense ID
     * @param userId the authenticated user ID (from SecurityContext)
     * @return the expense as response DTO
     * @throws ResourceNotFoundException if expense not found
     * @throws AccessDeniedException if expense does not belong to the authenticated user
     */
    ExpenseResponseDto getExpenseById(Long expenseId, Long userId);

    /**
     * Get all expenses for the authenticated user with pagination.
     * 
     * Query is automatically scoped to the authenticated user's ID.
     *
     * @param userId the authenticated user ID (from SecurityContext)
     * @param pageable pagination information
     * @return a page of expenses
     * @throws ResourceNotFoundException if user not found
     */
    Page<ExpenseResponseDto> getExpensesByUserId(Long userId, Pageable pageable);

    /**
     * Get expenses by category for the authenticated user.
     * 
     * Query is automatically scoped to the authenticated user's ID.
     *
     * @param userId the authenticated user ID (from SecurityContext)
     * @param category the expense category
     * @param pageable pagination information
     * @return a page of expenses
     * @throws ResourceNotFoundException if user not found
     */
    Page<ExpenseResponseDto> getExpensesByCategory(Long userId, ExpenseCategory category, Pageable pageable);

    /**
     * Get expenses within a date range for the authenticated user.
     * 
     * Query is automatically scoped to the authenticated user's ID.
     *
     * @param userId the authenticated user ID (from SecurityContext)
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of expenses
     * @throws ResourceNotFoundException if user not found
     * @throws ValidationException if date range is invalid
     */
    List<ExpenseResponseDto> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Update expense with ownership verification.
     * 
     * Throws AccessDeniedException if the expense does not belong to the authenticated user.
     *
     * @param expenseId the expense ID
     * @param userId the authenticated user ID (from SecurityContext)
     * @param expenseRequestDto the expense request DTO
     * @return the updated expense as response DTO
     * @throws ResourceNotFoundException if expense not found
     * @throws AccessDeniedException if expense does not belong to the authenticated user
     * @throws ValidationException if expense data is invalid
     */
    ExpenseResponseDto updateExpense(Long expenseId, Long userId, ExpenseRequestDto expenseRequestDto);

    /**
     * Delete expense with ownership verification.
     * 
     * Throws AccessDeniedException if the expense does not belong to the authenticated user.
     *
     * @param expenseId the expense ID
     * @param userId the authenticated user ID (from SecurityContext)
     * @throws ResourceNotFoundException if expense not found
     * @throws AccessDeniedException if expense does not belong to the authenticated user
     */
    void deleteExpense(Long expenseId, Long userId);

}
