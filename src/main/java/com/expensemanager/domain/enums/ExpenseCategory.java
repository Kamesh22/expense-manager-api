package com.expensemanager.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of expense categories.
 */
public enum ExpenseCategory {
    FOOD("Food & Dining"),
    TRANSPORTATION("Transportation"),
    UTILITIES("Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTHCARE("Healthcare"),
    SHOPPING("Shopping"),
    EDUCATION("Education"),
    TRAVEL("Travel"),
    OTHER("Other");

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static ExpenseCategory fromValue(String value) {
        if (value == null) {
            return null;
        }
        return ExpenseCategory.valueOf(value.toUpperCase());
    }
}
