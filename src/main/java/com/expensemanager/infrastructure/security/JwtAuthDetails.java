package com.expensemanager.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT Authentication details holder.
 */
@Getter
@AllArgsConstructor
public class JwtAuthDetails {
    private Long userId;
    private String username;
}
