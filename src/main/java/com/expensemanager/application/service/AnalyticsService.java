package com.expensemanager.application.service;

import com.expensemanager.application.dto.CategorySummaryDto;

import java.time.LocalDate;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Service interface for analytics operations.
 */
public interface AnalyticsService {

    /**
     * Get expense summary by category for a user.
     *
     * @param userId the user ID
     * @return category summary with totals
     */
    CategorySummaryDto getCategorySummary(Long userId);

    /**
     * Get monthly expense totals for a user.
     * Returns all months with expenses.
     *
     * @param userId the user ID
     * @return map of YYYY-MM formatted strings to BigDecimal totals
     */
    Map<String, BigDecimal> getMonthlySummary(Long userId);

    /**
     * Get monthly expense totals for a user within a specific date range.
     *
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return map of YYYY-MM formatted strings to BigDecimal totals
     */
    Map<String, BigDecimal> getMonthlySummaryByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

}
