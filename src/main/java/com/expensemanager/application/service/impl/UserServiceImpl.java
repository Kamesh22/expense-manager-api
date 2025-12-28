package com.expensemanager.application.service.impl;

import com.expensemanager.api.exception.ResourceNotFoundException;
import com.expensemanager.api.exception.ValidationException;
import com.expensemanager.application.dto.UserRequestDto;
import com.expensemanager.application.dto.UserResponseDto;
import com.expensemanager.application.mapper.EntityMapper;
import com.expensemanager.application.service.UserService;
import com.expensemanager.domain.entity.User;
import com.expensemanager.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserService.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityMapper entityMapper;

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.info("Creating new user with username: {}", userRequestDto.getUsername());

        if (userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User user = User.builder()
            .username(userRequestDto.getUsername())
            .email(userRequestDto.getEmail())
            .password(passwordEncoder.encode(userRequestDto.getPassword()))
            .role(userRequestDto.getRole())
            .isActive(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return entityMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return entityMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        log.debug("Fetching user with username: {}", username);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
            .map(entityMapper::toUserResponseDto);
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check if new username is already taken by another user
        if (!user.getUsername().equals(userRequestDto.getUsername()) &&
            userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        // Check if new email is already taken by another user
        if (!user.getEmail().equals(userRequestDto.getEmail()) &&
            userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        user.setUsername(userRequestDto.getUsername());
        user.setEmail(userRequestDto.getEmail());
        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }
        if (userRequestDto.getRole() != null) {
            user.setRole(userRequestDto.getRole());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return entityMapper.toUserResponseDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }

}
