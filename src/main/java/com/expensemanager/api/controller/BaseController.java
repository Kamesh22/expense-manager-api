package com.expensemanager.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base controller with common configurations.
 */
@RestController
@Tag(name = "API Controllers", description = "REST endpoints for Secure Expense Manager")
public abstract class BaseController {

    // Common constants and utilities can be added here

}
