package com.expensemanager.application.service;

import com.expensemanager.application.dto.AuthRequestDto;
import com.expensemanager.application.dto.AuthResponseDto;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Register a new user.
     *
     * @param authRequestDto the authentication request DTO
     * @return the authentication response with token
     */
    AuthResponseDto register(AuthRequestDto authRequestDto);

    /**
     * Login user and generate token.
     *
     * @param authRequestDto the authentication request DTO
     * @return the authentication response with token
     */
    AuthResponseDto login(AuthRequestDto authRequestDto);

}
