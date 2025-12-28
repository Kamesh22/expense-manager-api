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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of ExpenseService.
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
        log.info("Creating new expense for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate expense date is not in the future
        if (expenseRequestDto.getExpenseDate().isAfter(LocalDate.now())) {
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
        log.info("Expense created successfully with ID: {}", savedExpense.getId());

        return entityMapper.toExpenseResponseDto(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(Long id) {
        log.debug("Fetching expense with ID: {}", id);
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        return entityMapper.toExpenseResponseDto(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getExpensesByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching expenses for user: {} with pagination: {}", userId, pageable);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return expenseRepository.findByUser(user, pageable)
            .map(entityMapper::toExpenseResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getExpensesByCategory(Long userId, ExpenseCategory category, Pageable pageable) {
        log.debug("Fetching expenses for user: {} with category: {} and pagination: {}", userId, category, pageable);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return expenseRepository.findByUserAndCategory(user, category, pageable)
            .map(entityMapper::toExpenseResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching expenses for user: {} between dates: {} and {}", userId, startDate, endDate);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }

        return expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate)
            .stream()
            .map(entityMapper::toExpenseResponseDto)
            .toList();
    }

    @Override
    public ExpenseResponseDto updateExpense(Long id, ExpenseRequestDto expenseRequestDto) {
        log.info("Updating expense with ID: {}", id);

        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        // Validate expense date is not in the future
        if (expenseRequestDto.getExpenseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Expense date cannot be in the future");
        }

        expense.setAmount(expenseRequestDto.getAmount());
        expense.setCategory(expenseRequestDto.getCategory());
        expense.setDescription(expenseRequestDto.getDescription());
        expense.setExpenseDate(expenseRequestDto.getExpenseDate());

        Expense updatedExpense = expenseRepository.save(expense);
        log.info("Expense updated successfully with ID: {}", updatedExpense.getId());

        return entityMapper.toExpenseResponseDto(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id) {
        log.info("Deleting expense with ID: {}", id);

        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        expenseRepository.delete(expense);
        log.info("Expense deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean expenseExists(Long id) {
        return expenseRepository.existsById(id);
    }

}
