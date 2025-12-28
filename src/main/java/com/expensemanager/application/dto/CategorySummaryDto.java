package com.expensemanager.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for analytics category summary response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySummaryDto {

    /**
     * Map of category name to total amount.
     */
    private Map<String, BigDecimal> categoryTotals;

    /**
     * Total expenses across all categories.
     */
    private BigDecimal grandTotal;

}
