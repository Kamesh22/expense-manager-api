package com.expensemanager.application.mapper;

import com.expensemanager.domain.entity.User;
import com.expensemanager.application.dto.UserResponseDto;
import com.expensemanager.application.dto.ExpenseResponseDto;
import com.expensemanager.domain.entity.Expense;
import org.springframework.stereotype.Component;

/**
 * Utility class for mapping between entities and DTOs.
 */
@Component
public class EntityMapper {

    /**
     * Map User entity to UserResponseDto.
     */
    public UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    /**
     * Map Expense entity to ExpenseResponseDto.
     */
    public ExpenseResponseDto toExpenseResponseDto(Expense expense) {
        return ExpenseResponseDto.builder()
            .id(expense.getId())
            .userId(expense.getUser().getId())
            .amount(expense.getAmount())
            .category(expense.getCategory())
            .description(expense.getDescription())
            .expenseDate(expense.getExpenseDate())
            .createdAt(expense.getCreatedAt())
            .updatedAt(expense.getUpdatedAt())
            .build();
    }

}
