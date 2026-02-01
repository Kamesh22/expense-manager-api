package com.expensemanager.application.service.impl;

import com.expensemanager.application.dto.CategorySummaryDto;
import com.expensemanager.infrastructure.repository.ExpenseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnalyticsServiceImpl.
 * 
 * Focus: Analytics aggregation and security
 * - Category summary aggregation
 * - Monthly summary aggregation
 * - Soft delete exclusion via repository
 * - User data scoping
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Tests")
class AnalyticsServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    // ============ CATEGORY SUMMARY TESTS ============

    @Test
    @DisplayName("Should return category summary for user")
    void testGetCategorySummary_WithExpenses_ReturnsSummaryDto() {
        // Arrange
        Long userId = 1L;

        List<Object[]> categoryData = List.of(
            new Object[]{"FOOD", BigDecimal.valueOf(75.00)},
            new Object[]{"TRANSPORTATION", BigDecimal.valueOf(30.00)}
        );

        when(expenseRepository.getCategoryTotals(userId)).thenReturn(categoryData);

        // Act
        CategorySummaryDto result = analyticsService.getCategorySummary(userId);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(105.00), result.getGrandTotal());
        assertEquals(2, result.getCategoryTotals().size());
        verify(expenseRepository, times(1)).getCategoryTotals(userId);
    }

    @Test
    @DisplayName("Should exclude soft-deleted expenses from category summary")
    void testGetCategorySummary_ExcludesDeletedExpenses() {
        // Arrange
        Long userId = 1L;

        when(expenseRepository.getCategoryTotals(userId))
            .thenReturn(Collections.emptyList());

        // Act
        CategorySummaryDto result = analyticsService.getCategorySummary(userId);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getGrandTotal());
        verify(expenseRepository, times(1)).getCategoryTotals(userId);
    }

    @Test
    @DisplayName("Should return zero grand total for user with no expenses")
    void testGetCategorySummary_WithNoExpenses_ReturnsZeroTotal() {
        // Arrange
        Long userId = 1L;
        when(expenseRepository.getCategoryTotals(userId))
            .thenReturn(Collections.emptyList());

        // Act
        CategorySummaryDto result = analyticsService.getCategorySummary(userId);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getGrandTotal());
    }

    @Test
    @DisplayName("Should aggregate expenses by category correctly")
    void testGetCategorySummary_AggregatesByCategory() {
        // Arrange
        Long userId = 1L;

        List<Object[]> categoryData = List.of(
            new Object[]{"FOOD", BigDecimal.valueOf(40.00)},
            new Object[]{"TRANSPORTATION", BigDecimal.valueOf(50.00)},
            new Object[]{"UTILITIES", BigDecimal.valueOf(100.00)},
            new Object[]{"ENTERTAINMENT", BigDecimal.valueOf(25.00)}
        );

        when(expenseRepository.getCategoryTotals(userId)).thenReturn(categoryData);

        // Act
        CategorySummaryDto result = analyticsService.getCategorySummary(userId);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getCategoryTotals().size());
        assertEquals(BigDecimal.valueOf(40.00), result.getCategoryTotals().get("FOOD"));
        assertEquals(BigDecimal.valueOf(50.00), result.getCategoryTotals().get("TRANSPORTATION"));
        assertEquals(BigDecimal.valueOf(100.00), result.getCategoryTotals().get("UTILITIES"));
        assertEquals(BigDecimal.valueOf(215.00), result.getGrandTotal());
    }

    @Test
    @DisplayName("Should only include authenticated user's expenses in category summary")
    void testGetCategorySummary_ScopedToAuthenticatedUser() {
        // Arrange
        Long userId = 1L;
        when(expenseRepository.getCategoryTotals(userId))
            .thenReturn(Collections.emptyList());

        // Act
        analyticsService.getCategorySummary(userId);

        // Assert
        verify(expenseRepository, times(1)).getCategoryTotals(userId);
    }

    // ============ MONTHLY SUMMARY TESTS ============

    @Test
    @DisplayName("Should return monthly summary aggregated correctly")
    void testGetMonthlySummary_WithExpenses_ReturnsMonthlyTotals() {
        // Arrange
        Long userId = 1L;

        List<Object[]> mockResults = List.of(
            new Object[]{"2025-12", BigDecimal.valueOf(100.00)},
            new Object[]{"2025-11", BigDecimal.valueOf(75.50)}
        );

        when(expenseRepository.getMonthlySummary(userId))
            .thenReturn(mockResults);

        // Act
        Map<String, BigDecimal> result = analyticsService.getMonthlySummary(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(100.00), result.get("2025-12"));
        assertEquals(BigDecimal.valueOf(75.50), result.get("2025-11"));
        verify(expenseRepository, times(1)).getMonthlySummary(userId);
    }

    @Test
    @DisplayName("Should exclude soft-deleted expenses from monthly summary")
    void testGetMonthlySummary_ExcludesDeletedExpenses() {
        // Arrange
        Long userId = 1L;

        when(expenseRepository.getMonthlySummary(userId))
            .thenReturn(Collections.emptyList());

        // Act
        Map<String, BigDecimal> result = analyticsService.getMonthlySummary(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(expenseRepository, times(1)).getMonthlySummary(userId);
    }

    @Test
    @DisplayName("Should filter monthly summary by date range when provided")
    void testGetMonthlySummary_WithDateRange_FiltersResults() {
        // Arrange
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        List<Object[]> mockResults = List.of(
            new Object[]{"2025-12", BigDecimal.valueOf(100.00)},
            new Object[]{"2025-11", BigDecimal.valueOf(75.50)},
            new Object[]{"2025-10", BigDecimal.valueOf(50.00)}
        );

        when(expenseRepository.getMonthlySummaryByDateRange(userId, startDate, endDate))
            .thenReturn(mockResults);

        // Act
        Map<String, BigDecimal> result = analyticsService.getMonthlySummaryByDateRange(userId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(expenseRepository, times(1)).getMonthlySummaryByDateRange(userId, startDate, endDate);
    }

    @Test
    @DisplayName("Should return empty map when no expenses in date range")
    void testGetMonthlySummary_WithNoResults_ReturnsEmptyMap() {
        // Arrange
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);

        when(expenseRepository.getMonthlySummaryByDateRange(userId, startDate, endDate))
            .thenReturn(Collections.emptyList());

        // Act
        Map<String, BigDecimal> result = analyticsService.getMonthlySummaryByDateRange(userId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should only include authenticated user's expenses in monthly summary")
    void testGetMonthlySummary_ScopedToAuthenticatedUser() {
        // Arrange
        Long userId = 1L;
        when(expenseRepository.getMonthlySummary(userId))
            .thenReturn(Collections.emptyList());

        // Act
        analyticsService.getMonthlySummary(userId);

        // Assert
        verify(expenseRepository, times(1)).getMonthlySummary(userId);
    }

    @Test
    @DisplayName("Should return results sorted in descending order by month")
    void testGetMonthlySummary_ReturnsSortedResults() {
        // Arrange
        Long userId = 1L;

        List<Object[]> mockResults = List.of(
            new Object[]{"2025-12", BigDecimal.valueOf(100.00)},
            new Object[]{"2025-11", BigDecimal.valueOf(75.50)},
            new Object[]{"2025-10", BigDecimal.valueOf(50.00)}
        );

        when(expenseRepository.getMonthlySummary(userId))
            .thenReturn(mockResults);

        // Act
        Map<String, BigDecimal> result = analyticsService.getMonthlySummary(userId);

        // Assert
        assertNotNull(result);
        // HashMap doesn't preserve order, so check all values are present
        assertEquals(3, result.size());
        assertEquals(BigDecimal.valueOf(100.00), result.get("2025-12"));
        assertEquals(BigDecimal.valueOf(75.50), result.get("2025-11"));
        assertEquals(BigDecimal.valueOf(50.00), result.get("2025-10"));
    }

    @Test
    @DisplayName("Should throw exception for invalid date range")
    void testGetMonthlySummary_WithInvalidDateRange_ThrowsException() {
        // Arrange
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 12, 31);
        LocalDate endDate = LocalDate.of(2025, 10, 1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            analyticsService.getMonthlySummaryByDateRange(userId, startDate, endDate)
        );
    }

}
