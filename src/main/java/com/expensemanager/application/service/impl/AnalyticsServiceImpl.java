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

}
