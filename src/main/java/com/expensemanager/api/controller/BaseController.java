package com.expensemanager.api.controller;

import com.expensemanager.infrastructure.security.JwtAuthDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base controller with common configurations and utility methods.
 */
@RestController
@Tag(name = "API Controllers", description = "REST endpoints for Secure Expense Manager")
@Slf4j
public abstract class BaseController {

    /**
     * Extract authenticated user ID from authentication context.
     * Retrieves the userId from JwtAuthDetails stored in the Authentication object.
     *
     * @param authentication Spring Security Authentication object
     * @return the authenticated user's ID
     * @throws IllegalArgumentException if authentication is invalid or userId is missing
     */
    protected Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null) {
            log.warn("Invalid authentication: authentication object is null");
            throw new IllegalArgumentException("Authentication is required");
        }

        try {
            // Extract JwtAuthDetails from authentication
            if (authentication.getDetails() instanceof JwtAuthDetails) {
                JwtAuthDetails authDetails = (JwtAuthDetails) authentication.getDetails();
                Long userId = authDetails.getUserId();
                
                if (userId == null) {
                    log.warn("Invalid authentication: userId is null in JwtAuthDetails");
                    throw new IllegalArgumentException("User ID not found in authentication");
                }
                
                log.debug("Extracted authenticated user ID: {}", userId);
                return userId;
            } else {
                log.warn("Invalid authentication: details are not JwtAuthDetails, type: {}", 
                    authentication.getDetails() != null ? authentication.getDetails().getClass().getName() : "null");
                throw new IllegalArgumentException("Invalid authentication token");
            }
        } catch (ClassCastException e) {
            log.warn("Error extracting user ID from authentication: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid authentication token", e);
        }
    }

    /**
     * Extract authenticated user ID from SecurityContext.
     * This is a convenience method for use in controllers where Authentication is available in SecurityContext.
     *
     * @return the authenticated user's ID
     * @throws IllegalArgumentException if authentication is invalid or userId is missing
     */
    protected Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getAuthenticatedUserId(authentication);
    }

}
