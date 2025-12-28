package com.expensemanager.application.service;

import com.expensemanager.application.dto.CategorySummaryDto;

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

}
