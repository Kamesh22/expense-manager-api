package com.expensemanager.infrastructure.config;

import com.expensemanager.domain.entity.User;
import com.expensemanager.domain.enums.Role;
import com.expensemanager.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bootstrap component to create a default ADMIN user on application startup if none exists.
 * This runs only once and is idempotent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@expensemanager.local";
    private static final String ADMIN_PASSWORD = "AdminSecure123!";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createDefaultAdminIfNotExists();
    }

    /**
     * Creates a default ADMIN user if no ADMIN users exist in the system.
     * This is idempotent and runs only once.
     */
    private void createDefaultAdminIfNotExists() {
        // Check if any ADMIN user already exists
        boolean adminExists = userRepository.findByRole(Role.ADMIN).stream().findAny().isPresent();

        if (!adminExists) {
            log.info("No ADMIN user found. Creating default ADMIN user...");

            User adminUser = User.builder()
                .username(ADMIN_USERNAME)
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .isActive(true)
                .build();

            userRepository.save(adminUser);

            log.info("✓ Default ADMIN user created successfully");
            log.warn("⚠ IMPORTANT: Change default ADMIN password immediately!");
            log.warn("  Username: {}", ADMIN_USERNAME);
            log.warn("  Email: {}", ADMIN_EMAIL);
            log.warn("  TEMPORARY Password: {}", ADMIN_PASSWORD);
        } else {
            log.debug("ADMIN user already exists. Skipping bootstrap.");
        }
    }

}
