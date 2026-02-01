package com.expensemanager.infrastructure.security;

import com.expensemanager.domain.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * Utility for converting JWT roles to Spring Security GrantedAuthorities.
 * Provides clean separation between JWT role claims and Spring Security authorities.
 */
@Component
public class JwtAuthorityUtil {

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Convert a role from JWT to Spring Security GrantedAuthority.
     *
     * @param role the Role enum from JWT
     * @return Collection of GrantedAuthority with proper ROLE_ prefix
     */
    public Collection<GrantedAuthority> convertToAuthorities(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }

        String authority = ROLE_PREFIX + role.name();
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    /**
     * Convert role string (from JWT claim) to GrantedAuthority.
     * Useful if role is stored as string in token.
     *
     * @param roleString the role as string (e.g., "USER", "ADMIN", "VIEWER")
     * @return Collection of GrantedAuthority
     */
    public Collection<GrantedAuthority> convertToAuthoritiesByString(String roleString) {
        if (roleString == null || roleString.isBlank()) {
            return Collections.emptyList();
        }

        try {
            Role role = Role.valueOf(roleString.toUpperCase());
            return convertToAuthorities(role);
        } catch (IllegalArgumentException e) {
            // Invalid role string, return empty authorities
            return Collections.emptyList();
        }
    }

}
