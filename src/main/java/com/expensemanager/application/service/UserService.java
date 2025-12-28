package com.expensemanager.application.service;

import com.expensemanager.domain.entity.User;
import com.expensemanager.application.dto.UserRequestDto;
import com.expensemanager.application.dto.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for User operations.
 */
public interface UserService {

    /**
     * Create a new user.
     *
     * @param userRequestDto the user request DTO
     * @return the created user as response DTO
     */
    UserResponseDto createUser(UserRequestDto userRequestDto);

    /**
     * Get user by ID.
     *
     * @param id the user ID
     * @return the user as response DTO
     */
    UserResponseDto getUserById(Long id);

    /**
     * Get user by username.
     *
     * @param username the username
     * @return the user entity
     */
    User getUserByUsername(String username);

    /**
     * Get all users with pagination.
     *
     * @param pageable pagination information
     * @return a page of users
     */
    Page<UserResponseDto> getAllUsers(Pageable pageable);

    /**
     * Update user information.
     *
     * @param id the user ID
     * @param userRequestDto the user request DTO
     * @return the updated user as response DTO
     */
    UserResponseDto updateUser(Long id, UserRequestDto userRequestDto);

    /**
     * Delete user by ID.
     *
     * @param id the user ID
     */
    void deleteUser(Long id);

    /**
     * Check if user exists by ID.
     *
     * @param id the user ID
     * @return true if user exists, false otherwise
     */
    boolean userExists(Long id);

}
