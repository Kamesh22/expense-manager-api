package com.expensemanager.application.service.impl;

import com.expensemanager.application.dto.CategorySummaryDto;
import com.expensemanager.application.service.AnalyticsService;
import com.expensemanager.domain.enums.ExpenseCategory;
import com.expensemanager.infrastructure.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AnalyticsService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ExpenseRepository expenseRepository;

    @Override
    public CategorySummaryDto getCategorySummary(Long userId) {
        log.debug("Generating category summary for user: {}", userId);

        // Get aggregated expenses by category using JPQL query
        List<Object[]> results = expenseRepository.getCategoryTotals(userId);
        
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Object[] result : results) {
            String category;
            Object categoryObj = result[0];
            
            // Handle both enum and string returns from the query
            if (categoryObj instanceof ExpenseCategory) {
                category = ((ExpenseCategory) categoryObj).name();
            } else {
                category = (String) categoryObj;
            }
            
            BigDecimal total = (BigDecimal) result[1];
            categoryTotals.put(category, total);
            grandTotal = grandTotal.add(total);
        }

        log.debug("Category summary generated with {} categories and total: {}", 
            categoryTotals.size(), grandTotal);

        return CategorySummaryDto.builder()
            .categoryTotals(categoryTotals)
            .grandTotal(grandTotal)
            .build();
    }

    @Override
    public Map<String, BigDecimal> getMonthlySummary(Long userId) {
        log.debug("Generating monthly summary for user: {}", userId);

        List<Object[]> results = expenseRepository.getMonthlySummary(userId);
        Map<String, BigDecimal> monthlySummary = new HashMap<>();

        for (Object[] result : results) {
            String yearMonth = (String) result[0];
            BigDecimal total = (BigDecimal) result[1];
            monthlySummary.put(yearMonth, total);
        }

        log.debug("Monthly summary generated with {} months of data", monthlySummary.size());
        return monthlySummary;
    }

    @Override
    public Map<String, BigDecimal> getMonthlySummaryByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Generating monthly summary for user: {} between {} and {}", userId, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            log.warn("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        List<Object[]> results = expenseRepository.getMonthlySummaryByDateRange(userId, startDate, endDate);
        Map<String, BigDecimal> monthlySummary = new HashMap<>();

        for (Object[] result : results) {
            String yearMonth = (String) result[0];
            BigDecimal total = (BigDecimal) result[1];
            monthlySummary.put(yearMonth, total);
        }

        log.debug("Monthly summary generated with {} months of data in the specified range", monthlySummary.size());
        return monthlySummary;
    }

}
