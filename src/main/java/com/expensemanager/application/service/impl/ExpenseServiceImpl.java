package com.expensemanager.application.service.impl;

import com.expensemanager.api.exception.ResourceNotFoundException;
import com.expensemanager.api.exception.ValidationException;
import com.expensemanager.application.dto.ExpenseRequestDto;
import com.expensemanager.application.dto.ExpenseResponseDto;
import com.expensemanager.application.mapper.EntityMapper;
import com.expensemanager.application.service.ExpenseService;
import com.expensemanager.domain.entity.Expense;
import com.expensemanager.domain.entity.User;
import com.expensemanager.domain.enums.ExpenseCategory;
import com.expensemanager.infrastructure.repository.ExpenseRepository;
import com.expensemanager.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of ExpenseService with strict ownership enforcement.
 * 
 * CRITICAL: All operations verify that the expense belongs to the authenticated user.
 * Any attempt to access another user's expense results in AccessDeniedException (HTTP 403).
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Override
    public ExpenseResponseDto createExpense(Long userId, ExpenseRequestDto expenseRequestDto) {
        log.info("Creating new expense for authenticated user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", userId);
                return new ResourceNotFoundException("User not found with ID: " + userId);
            });

        // Validate expense date is not in the future
        if (expenseRequestDto.getExpenseDate().isAfter(LocalDate.now())) {
            log.warn("Expense date is in the future: {}", expenseRequestDto.getExpenseDate());
            throw new ValidationException("Expense date cannot be in the future");
        }

        Expense expense = Expense.builder()
            .user(user)
            .amount(expenseRequestDto.getAmount())
            .category(expenseRequestDto.getCategory())
            .description(expenseRequestDto.getDescription())
            .expenseDate(expenseRequestDto.getExpenseDate())
            .build();

        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense created successfully with ID: {} for user: {}", savedExpense.getId(), userId);

        return entityMapper.toExpenseResponseDto(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(Long expenseId, Long userId) {
        log.debug("Fetching expense with ID: {} for user: {}", expenseId, userId);

        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> {
                log.error("Expense not found with ID: {}", expenseId);
                return new ResourceNotFoundException("Expense not found with ID: " + expenseId);
            });

        // OWNERSHIP CHECK: Verify expense belongs to authenticated user
        verifyExpenseOwnership(expense, userId);

        return entityMapper.toExpenseResponseDto(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getExpensesByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching expenses for authenticated user: {} with pagination: {}", userId, pageable);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", userId);
                return new ResourceNotFoundException("User not found with ID: " + userId);
            });

        // Query automatically scoped to user - no additional ownership check needed
        return expenseRepository.findByUser(user, pageable)
            .map(entityMapper::toExpenseResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getExpensesByCategory(Long userId, ExpenseCategory category, Pageable pageable) {
        log.debug("Fetching expenses for user: {} with category: {} and pagination: {}", userId, category, pageable);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", userId);
                return new ResourceNotFoundException("User not found with ID: " + userId);
            });

        // Query automatically scoped to user - no additional ownership check needed
        return expenseRepository.findByUserAndCategory(user, category, pageable)
            .map(entityMapper::toExpenseResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching expenses for user: {} between dates: {} and {}", userId, startDate, endDate);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", userId);
                return new ResourceNotFoundException("User not found with ID: " + userId);
            });

        if (startDate.isAfter(endDate)) {
            log.warn("Start date is after end date: {} > {}", startDate, endDate);
            throw new ValidationException("Start date must be before end date");
        }

        // Query automatically scoped to user - no additional ownership check needed
        return expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate)
            .stream()
            .map(entityMapper::toExpenseResponseDto)
            .toList();
    }

    @Override
    public ExpenseResponseDto updateExpense(Long expenseId, Long userId, ExpenseRequestDto expenseRequestDto) {
        log.info("Updating expense with ID: {} for user: {}", expenseId, userId);

        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> {
                log.error("Expense not found with ID: {}", expenseId);
                return new ResourceNotFoundException("Expense not found with ID: " + expenseId);
            });

        // OWNERSHIP CHECK: Verify expense belongs to authenticated user
        verifyExpenseOwnership(expense, userId);

        // Validate expense date is not in the future
        if (expenseRequestDto.getExpenseDate().isAfter(LocalDate.now())) {
            log.warn("Update attempt with future date: {}", expenseRequestDto.getExpenseDate());
            throw new ValidationException("Expense date cannot be in the future");
        }

        expense.setAmount(expenseRequestDto.getAmount());
        expense.setCategory(expenseRequestDto.getCategory());
        expense.setDescription(expenseRequestDto.getDescription());
        expense.setExpenseDate(expenseRequestDto.getExpenseDate());

        Expense updatedExpense = expenseRepository.save(expense);
        log.info("Expense updated successfully with ID: {} for user: {}", updatedExpense.getId(), userId);

        return entityMapper.toExpenseResponseDto(updatedExpense);
    }

    @Override
    public void deleteExpense(Long expenseId, Long userId) {
        log.info("Deleting expense with ID: {} for user: {}", expenseId, userId);

        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> {
                log.error("Expense not found with ID: {}", expenseId);
                return new ResourceNotFoundException("Expense not found with ID: " + expenseId);
            });

        // OWNERSHIP CHECK: Verify expense belongs to authenticated user
        verifyExpenseOwnership(expense, userId);

        // SOFT DELETE: Mark as deleted instead of physically removing the record
        expense.setIsDeleted(true);
        expense.setDeletedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense);
        log.info("Expense soft-deleted successfully with ID: {} for user: {} at {}", 
            expenseId, userId, LocalDateTime.now());
    }

    /**
     * Verify that an expense belongs to the authenticated user.
     * 
     * CRITICAL SECURITY CHECK: Throws AccessDeniedException if the expense does not belong to the user.
     * This prevents unauthorized access to other users' expenses.
     *
     * @param expense the expense entity
     * @param userId the authenticated user ID
     * @throws AccessDeniedException if the expense does not belong to the authenticated user
     */
    private void verifyExpenseOwnership(Expense expense, Long userId) {
        if (!expense.getUser().getId().equals(userId)) {
            log.warn("SECURITY: User {} attempted to access expense {} which belongs to user {}",
                userId, expense.getId(), expense.getUser().getId());
            throw new AccessDeniedException("You do not have permission to access this expense");
        }
    }

}
