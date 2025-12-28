package com.expensemanager.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of user roles in the system.
 */
public enum Role {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    VIEWER("ROLE_VIEWER");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Role.valueOf(value.toUpperCase());
    }
}
