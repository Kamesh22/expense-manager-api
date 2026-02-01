package com.expensemanager.application.dto;

import com.expensemanager.domain.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for admin role change operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleChangeRequestDto {

    @NotNull(message = "Role cannot be null")
    private Role role;

}
