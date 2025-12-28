package com.expensemanager.application.service.impl;

import com.expensemanager.api.exception.ValidationException;
import com.expensemanager.application.dto.AuthRequestDto;
import com.expensemanager.application.dto.AuthResponseDto;
import com.expensemanager.application.mapper.EntityMapper;
import com.expensemanager.application.service.AuthService;
import com.expensemanager.domain.entity.User;
import com.expensemanager.domain.enums.Role;
import com.expensemanager.infrastructure.repository.UserRepository;
import com.expensemanager.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService with JWT token generation.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EntityMapper entityMapper;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public AuthResponseDto register(AuthRequestDto authRequestDto) {
        log.info("Registering new user with username: {}", authRequestDto.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(authRequestDto.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(authRequestDto.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Create new user
        User user = User.builder()
            .username(authRequestDto.getUsername())
            .email(authRequestDto.getEmail())
            .password(passwordEncoder.encode(authRequestDto.getPassword()))
            .role(Role.USER)
            .isActive(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getUsername());

        return AuthResponseDto.builder()
            .userId(savedUser.getId())
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .role(savedUser.getRole())
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtExpiration)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(AuthRequestDto authRequestDto) {
        log.info("Login attempt for user: {}", authRequestDto.getUsername());

        // Find user by username
        User user = userRepository.findByUsername(authRequestDto.getUsername())
            .orElseThrow(() -> new ValidationException("Invalid username or password"));

        // Validate password
        if (!passwordEncoder.matches(authRequestDto.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid username or password");
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new ValidationException("User account is inactive");
        }

        log.info("User logged in successfully: {}", user.getId());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        return AuthResponseDto.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtExpiration)
            .build();
    }

}
