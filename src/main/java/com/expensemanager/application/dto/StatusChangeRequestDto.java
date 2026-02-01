package com.expensemanager.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for admin status change operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusChangeRequestDto {

    @NotNull(message = "Status cannot be null")
    private Boolean isActive;

}
