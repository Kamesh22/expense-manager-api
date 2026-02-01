package com.expensemanager.api.controller;

import com.expensemanager.application.dto.CategorySummaryDto;
import com.expensemanager.application.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * REST Controller for Analytics operations.
 * 
 * All analytics are scoped to the authenticated user.
 * Base path: /api/v1/analytics
 */
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for expense analytics")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class AnalyticsController extends BaseController {

    private final AnalyticsService analyticsService;

    /**
     * Get category summary for the authenticated user.
     * 
     * Returns expense totals grouped by category.
     * User ID is extracted from JWT token.
     *
     * @param authentication Spring Security authentication object
     * @return category summary with totals
     */
    @GetMapping("/category-summary")
    @Operation(
        summary = "Get category summary",
        description = "Retrieve expense totals grouped by category for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category summary retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<CategorySummaryDto> getCategorySummary(Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        log.debug("Fetching category summary for authenticated user: {}", userId);
        
        CategorySummaryDto summary = analyticsService.getCategorySummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get monthly expense summary for the authenticated user.
     * 
     * Returns expense totals grouped by month (YYYY-MM format).
     * Supports optional date range filtering.
     *
     * @param authentication Spring Security authentication object
     * @param startDate optional start date (inclusive)
     * @param endDate optional end date (inclusive)
     * @return map of YYYY-MM to BigDecimal amounts
     */
    @GetMapping("/monthly-summary")
    @Operation(
        summary = "Get monthly expense summary",
        description = "Retrieve expense totals grouped by month for the authenticated user. Optionally filter by date range."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Monthly summary retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range - start date after end date"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    public ResponseEntity<Map<String, BigDecimal>> getMonthlySummary(
            Authentication authentication,
            @RequestParam(required = false)
            @Parameter(description = "Start date (YYYY-MM-DD format), inclusive. Optional.")
            LocalDate startDate,
            @RequestParam(required = false)
            @Parameter(description = "End date (YYYY-MM-DD format), inclusive. Optional.")
            LocalDate endDate) {
        
        Long userId = getAuthenticatedUserId(authentication);
        log.debug("Fetching monthly summary for authenticated user: {} with date range: {} to {}", 
            userId, startDate, endDate);
        
        Map<String, BigDecimal> summary;
        
        if (startDate != null && endDate != null) {
            summary = analyticsService.getMonthlySummaryByDateRange(userId, startDate, endDate);
        } else if (startDate != null || endDate != null) {
            log.warn("Incomplete date range provided - ignoring partial range");
            summary = analyticsService.getMonthlySummary(userId);
        } else {
            summary = analyticsService.getMonthlySummary(userId);
        }
        
        return ResponseEntity.ok(summary);
    }

}
