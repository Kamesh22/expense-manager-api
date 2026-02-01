package com.expensemanager.application.service.impl;

import com.expensemanager.api.exception.ValidationException;
import com.expensemanager.application.dto.AuthRequestDto;
import com.expensemanager.application.dto.AuthResponseDto;
import com.expensemanager.application.mapper.EntityMapper;
import com.expensemanager.domain.entity.User;
import com.expensemanager.domain.enums.Role;
import com.expensemanager.infrastructure.repository.UserRepository;
import com.expensemanager.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 * 
 * Focus: Authentication and security behavior
 * - User registration with default USER role
 * - Password encoding
 * - Duplicate username/email rejection
 * - Login success and failure
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    // Test Data Setup
    private User createTestUser(Long id, String username, String email, String password, Role role) {
        return User.builder()
            .id(id)
            .username(username)
            .email(email)
            .password(password)
            .role(role)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
    }

    // ============ REGISTRATION TESTS ============

    @Test
    @DisplayName("Should register new user with default USER role")
    void testRegister_WithValidRequest_CreatesUserWithUserRole() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("john_doe")
            .email("john@example.com")
            .password("SecurePass123!")
            .build();

        String encodedPassword = "encoded_password_hash";
        User savedUser = createTestUser(1L, request.getUsername(), request.getEmail(), 
            encodedPassword, Role.USER);

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(1L, request.getUsername(), Role.USER.name()))
            .thenReturn("jwt_token");

        // Act
        AuthResponseDto result = authService.register(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(argThat(user ->
            user.getUsername().equals(request.getUsername()) &&
            user.getEmail().equals(request.getEmail()) &&
            user.getRole().equals(Role.USER) &&
            user.getIsActive()
        ));
    }

    @Test
    @DisplayName("Should encode password using PasswordEncoder")
    void testRegister_EncodesPasswordSecurely() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("john_doe")
            .email("john@example.com")
            .password("PlainPassword123!")
            .build();

        String encodedPassword = "bcrypt_hash_xyz";

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(
            createTestUser(1L, request.getUsername(), request.getEmail(), 
                encodedPassword, Role.USER)
        );
        when(jwtTokenProvider.generateToken(1L, request.getUsername(), Role.USER.name()))
            .thenReturn("jwt_token");

        // Act
        authService.register(request);

        // Assert
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(argThat(user ->
            user.getPassword().equals(encodedPassword)
        ));
    }

    @Test
    @DisplayName("Should reject duplicate username")
    void testRegister_WithDuplicateUsername_ThrowsValidationException() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("john_doe")
            .email("john@example.com")
            .password("SecurePass123!")
            .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject duplicate email")
    void testRegister_WithDuplicateEmail_ThrowsValidationException() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("john_doe")
            .email("john@example.com")
            .password("SecurePass123!")
            .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should return JWT token with role claim on successful registration")
    void testRegister_ReturnsJwtWithRoleClaim() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("john_doe")
            .email("john@example.com")
            .password("SecurePass123!")
            .build();

        User savedUser = createTestUser(1L, request.getUsername(), request.getEmail(),
            "encoded_password", Role.USER);

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(1L, "john_doe", Role.USER.name()))
            .thenReturn("jwt_token_with_role");

        // Act
        authService.register(request);

        // Assert
        verify(jwtTokenProvider, times(1)).generateToken(1L, request.getUsername(), "USER");
    }

    // ============ LOGIN TESTS ============

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_WithValidCredentials_ReturnsJwtToken() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("john_doe")
            .password("CorrectPassword123!")
            .build();

        User user = createTestUser(1L, "john_doe", "john@example.com",
            "encoded_password_hash", Role.USER);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "john_doe", Role.USER.name()))
            .thenReturn("jwt_token");

        // Act
        AuthResponseDto result = authService.login(request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
    }

    @Test
    @DisplayName("Should reject login with wrong password")
    void testLogin_WithWrongPassword_ThrowsValidationException() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("john_doe")
            .password("WrongPassword123!")
            .build();

        User user = createTestUser(1L, "john_doe", "john@example.com",
            "encoded_password_hash", Role.USER);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> authService.login(request));
        verify(jwtTokenProvider, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject login with non-existent username")
    void testLogin_WithNonExistentUser_ThrowsValidationException() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("nonexistent_user")
            .password("Password123!")
            .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ValidationException.class, () -> authService.login(request));
        verify(jwtTokenProvider, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should use user's actual role from database in JWT")
    void testLogin_IncludesUserRoleInJwt() {
        // Arrange
        AuthRequestDto request = AuthRequestDto.builder()
            .username("admin_user")
            .password("AdminPass123!")
            .build();

        User adminUser = createTestUser(5L, "admin_user", "admin@example.com",
            "encoded_password", Role.ADMIN);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(request.getPassword(), adminUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(5L, "admin_user", Role.ADMIN.name()))
            .thenReturn("admin_jwt_token");

        // Act
        authService.login(request);

        // Assert
        verify(jwtTokenProvider, times(1)).generateToken(5L, "admin_user", "ADMIN");
    }

    @Test
    @DisplayName("Should allow login for both USER and VIEWER roles")
    void testLogin_WorksForAllActiveRoles() {
        // Arrange - Test VIEWER role
        AuthRequestDto request = AuthRequestDto.builder()
            .username("viewer_user")
            .password("ViewerPass123!")
            .build();

        User viewerUser = createTestUser(2L, "viewer_user", "viewer@example.com",
            "encoded_password", Role.VIEWER);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(viewerUser));
        when(passwordEncoder.matches(request.getPassword(), viewerUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(2L, "viewer_user", Role.VIEWER.name()))
            .thenReturn("viewer_jwt_token");

        // Act
        AuthResponseDto result = authService.login(request);

        // Assert
        assertNotNull(result);
        verify(jwtTokenProvider, times(1)).generateToken(2L, "viewer_user", "VIEWER");
    }

}
