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
 */
public interface ExpenseService {

    /**
     * Create a new expense for a user.
     *
     * @param userId the user ID
     * @param expenseRequestDto the expense request DTO
     * @return the created expense as response DTO
     */
    ExpenseResponseDto createExpense(Long userId, ExpenseRequestDto expenseRequestDto);

    /**
     * Get expense by ID.
     *
     * @param id the expense ID
     * @return the expense as response DTO
     */
    ExpenseResponseDto getExpenseById(Long id);

    /**
     * Get all expenses for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return a page of expenses
     */
    Page<ExpenseResponseDto> getExpensesByUserId(Long userId, Pageable pageable);

    /**
     * Get expenses by category for a user.
     *
     * @param userId the user ID
     * @param category the expense category
     * @param pageable pagination information
     * @return a page of expenses
     */
    Page<ExpenseResponseDto> getExpensesByCategory(Long userId, ExpenseCategory category, Pageable pageable);

    /**
     * Get expenses within a date range.
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of expenses
     */
    List<ExpenseResponseDto> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Update expense information.
     *
     * @param id the expense ID
     * @param expenseRequestDto the expense request DTO
     * @return the updated expense as response DTO
     */
    ExpenseResponseDto updateExpense(Long id, ExpenseRequestDto expenseRequestDto);

    /**
     * Delete expense by ID.
     *
     * @param id the expense ID
     */
    void deleteExpense(Long id);

    /**
     * Check if expense exists by ID.
     *
     * @param id the expense ID
     * @return true if expense exists, false otherwise
     */
    boolean expenseExists(Long id);

}
