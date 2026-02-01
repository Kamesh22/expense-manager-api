package com.expensemanager.api.controller;

import com.expensemanager.application.dto.CategorySummaryDto;
import com.expensemanager.application.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

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

}
