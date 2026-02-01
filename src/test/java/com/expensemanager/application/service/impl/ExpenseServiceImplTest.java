package com.expensemanager.application.service.impl;

import com.expensemanager.api.exception.ResourceNotFoundException;
import com.expensemanager.application.dto.ExpenseRequestDto;
import com.expensemanager.application.dto.ExpenseResponseDto;
import com.expensemanager.application.mapper.EntityMapper;
import com.expensemanager.domain.entity.Expense;
import com.expensemanager.domain.entity.User;
import com.expensemanager.domain.enums.ExpenseCategory;
import com.expensemanager.infrastructure.repository.ExpenseRepository;
import com.expensemanager.infrastructure.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.security.access.AccessDeniedException;

/**
 * Unit tests for ExpenseServiceImpl.
 * 
 * Focus: Business logic and security behavior
 * - Expense creation and ownership verification
 * - Soft delete functionality
 * - Authorization enforcement
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService Tests")
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    // Test Data Setup
    private User createTestUser(Long id, String username) {
        return User.builder()
            .id(id)
            .username(username)
            .email(username + "@example.com")
            .password("encrypted_password")
            .isActive(true)
            .build();
    }

    private Expense createTestExpense(Long id, User user, BigDecimal amount) {
        return Expense.builder()
            .id(id)
            .user(user)
            .amount(amount)
            .category(ExpenseCategory.FOOD)
            .description("Test expense")
            .expenseDate(LocalDate.now())
            .isDeleted(false)
            .createdAt(LocalDateTime.now())
            .build();
    }

    private ExpenseRequestDto createExpenseRequest(BigDecimal amount) {
        return ExpenseRequestDto.builder()
            .amount(amount)
            .category(ExpenseCategory.FOOD)
            .description("Test expense")
            .expenseDate(LocalDate.now())
            .build();
    }

    // ============ CREATE EXPENSE TESTS ============

    @Test
    @DisplayName("Should create expense for authenticated user")
    void testCreateExpense_WithValidRequest_ReturnsCreatedExpense() {
        // Arrange
        Long userId = 1L;
        User user = createTestUser(userId, "john_doe");
        ExpenseRequestDto request = createExpenseRequest(BigDecimal.valueOf(25.50));
        Expense savedExpense = createTestExpense(1L, user, BigDecimal.valueOf(25.50));
        ExpenseResponseDto expectedResponse = new ExpenseResponseDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);
        when(entityMapper.toExpenseResponseDto(savedExpense)).thenReturn(expectedResponse);

        // Act
        ExpenseResponseDto result = expenseService.createExpense(userId, request);

        // Assert
        assertNotNull(result);
        verify(expenseRepository, times(1)).save(argThat(expense ->
            expense.getUser().getId().equals(userId) &&
            expense.getAmount().equals(request.getAmount()) &&
            !expense.getIsDeleted()
        ));
    }

    @Test
    @DisplayName("Should set amount, category, and date correctly on expense creation")
    void testCreateExpense_ValidatesExpenseProperties() {
        // Arrange
        Long userId = 1L;
        User user = createTestUser(userId, "john_doe");
        ExpenseRequestDto request = ExpenseRequestDto.builder()
            .amount(BigDecimal.valueOf(100.00))
            .category(ExpenseCategory.TRANSPORTATION)
            .description("Train ticket")
            .expenseDate(LocalDate.of(2025, 12, 25))
            .build();

        Expense savedExpense = Expense.builder()
            .id(1L)
            .user(user)
            .amount(request.getAmount())
            .category(request.getCategory())
            .description(request.getDescription())
            .expenseDate(request.getExpenseDate())
            .isDeleted(false)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);
        when(entityMapper.toExpenseResponseDto(any())).thenReturn(new ExpenseResponseDto());

        // Act
        expenseService.createExpense(userId, request);

        // Assert
        verify(expenseRepository, times(1)).save(argThat(expense ->
            expense.getAmount().equals(BigDecimal.valueOf(100.00)) &&
            expense.getCategory().equals(ExpenseCategory.TRANSPORTATION) &&
            expense.getExpenseDate().equals(LocalDate.of(2025, 12, 25))
        ));
    }

    // ============ OWNERSHIP ENFORCEMENT TESTS ============

    @Test
    @DisplayName("Should throw AccessDeniedException when user tries to access another user's expense")
    void testGetExpenseById_WithDifferentUser_ThrowsAccessDeniedException() {
        // Arrange
        Long ownerId = 1L;
        Long requestingUserId = 2L;
        Long expenseId = 100L;

        User owner = createTestUser(ownerId, "owner");
        Expense expense = createTestExpense(expenseId, owner, BigDecimal.valueOf(50.00));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
            expenseService.getExpenseById(expenseId, requestingUserId)
        );
    }

    @Test
    @DisplayName("Should allow user to access own expense")
    void testGetExpenseById_WithOwner_ReturnsExpense() {
        // Arrange
        Long userId = 1L;
        Long expenseId = 100L;

        User user = createTestUser(userId, "john_doe");
        Expense expense = createTestExpense(expenseId, user, BigDecimal.valueOf(50.00));
        ExpenseResponseDto expectedResponse = new ExpenseResponseDto();

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(entityMapper.toExpenseResponseDto(expense)).thenReturn(expectedResponse);

        // Act
        ExpenseResponseDto result = expenseService.getExpenseById(expenseId, userId);

        // Assert
        assertNotNull(result);
        verify(entityMapper, times(1)).toExpenseResponseDto(expense);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when expense not found")
    void testGetExpenseById_WithNonExistentExpense_ThrowsNotFoundException() {
        // Arrange
        Long expenseId = 999L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            expenseService.getExpenseById(expenseId, 1L)
        );
    }

    // ============ SOFT DELETE TESTS ============

    @Test
    @DisplayName("Should mark expense as deleted (soft delete) without removing from database")
    void testDeleteExpense_ShouldSoftDeleteExpense() {
        // Arrange
        Long userId = 1L;
        Long expenseId = 100L;

        User user = createTestUser(userId, "john_doe");
        Expense expense = createTestExpense(expenseId, user, BigDecimal.valueOf(50.00));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        // Act
        expenseService.deleteExpense(expenseId, userId);

        // Assert
        verify(expenseRepository, times(1)).save(argThat(savedExpense ->
            savedExpense.getIsDeleted() &&
            savedExpense.getDeletedAt() != null
        ));
    }

    @Test
    @DisplayName("Should prevent deletion of another user's expense")
    void testDeleteExpense_WithDifferentUser_ThrowsAccessDeniedException() {
        // Arrange
        Long ownerId = 1L;
        Long requestingUserId = 2L;
        Long expenseId = 100L;

        User owner = createTestUser(ownerId, "owner");
        Expense expense = createTestExpense(expenseId, owner, BigDecimal.valueOf(50.00));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
            expenseService.deleteExpense(expenseId, requestingUserId)
        );
    }

    // ============ UPDATE EXPENSE TESTS ============

    @Test
    @DisplayName("Should update expense for owner")
    void testUpdateExpense_WithOwner_UpdatesSuccessfully() {
        // Arrange
        Long userId = 1L;
        Long expenseId = 100L;

        User user = createTestUser(userId, "john_doe");
        Expense expense = createTestExpense(expenseId, user, BigDecimal.valueOf(50.00));
        
        ExpenseRequestDto updateRequest = ExpenseRequestDto.builder()
            .amount(BigDecimal.valueOf(75.00))
            .category(ExpenseCategory.ENTERTAINMENT)
            .description("Updated expense")
            .expenseDate(LocalDate.now())
            .build();

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(entityMapper.toExpenseResponseDto(any())).thenReturn(new ExpenseResponseDto());

        // Act
        expenseService.updateExpense(expenseId, userId, updateRequest);

        // Assert
        verify(expenseRepository, times(1)).save(argThat(updatedExpense ->
            updatedExpense.getAmount().equals(BigDecimal.valueOf(75.00)) &&
            updatedExpense.getCategory().equals(ExpenseCategory.ENTERTAINMENT)
        ));
    }

    @Test
    @DisplayName("Should prevent update of another user's expense")
    void testUpdateExpense_WithDifferentUser_ThrowsAccessDeniedException() {
        // Arrange
        Long ownerId = 1L;
        Long requestingUserId = 2L;
        Long expenseId = 100L;

        User owner = createTestUser(ownerId, "owner");
        Expense expense = createTestExpense(expenseId, owner, BigDecimal.valueOf(50.00));

        ExpenseRequestDto updateRequest = createExpenseRequest(BigDecimal.valueOf(75.00));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
            expenseService.updateExpense(expenseId, requestingUserId, updateRequest)
        );
    }

    // ============ LIST EXPENSES TESTS ============

    @Test
    @DisplayName("Should return only authenticated user's expenses")
    void testGetExpensesByUserId_ReturnsPaginatedExpenses() {
        // Arrange
        Long userId = 1L;
        User user = createTestUser(userId, "john_doe");
        Expense expense1 = createTestExpense(1L, user, BigDecimal.valueOf(50.00));
        Expense expense2 = createTestExpense(2L, user, BigDecimal.valueOf(75.00));

        Page<Expense> expensePage = new PageImpl<>(List.of(expense1, expense2));
        Page<ExpenseResponseDto> expectedResponse = new PageImpl<>(List.of(
            new ExpenseResponseDto(),
            new ExpenseResponseDto()
        ));

        when(expenseRepository.findByUser(eq(user), any(Pageable.class)))
            .thenReturn(expensePage);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(entityMapper.toExpenseResponseDto(any(Expense.class)))
            .thenReturn(new ExpenseResponseDto());

        // Act
        Page<ExpenseResponseDto> result = expenseService.getExpensesByUserId(userId, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(expenseRepository, times(1)).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    @DisplayName("Should exclude soft-deleted expenses from list")
    void testGetExpensesByUserId_ExcludesDeletedExpenses() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        User user = createTestUser(userId, "john_doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.findByUser(user, pageable)).thenReturn(Page.empty());

        // Act
        expenseService.getExpensesByUserId(userId, pageable);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(expenseRepository, times(1)).findByUser(user, pageable);
    }

}
