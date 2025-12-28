package com.expensemanager.application.dto;

import com.expensemanager.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {

    private Long userId;
    private String username;
    private String email;
    private Role role;
    private String accessToken;
    private String tokenType;
    private Long expiresIn;

}
