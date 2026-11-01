package com.campus.lostfound.config;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.Role;
import com.campus.lostfound.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Idempotent, configuration-guarded provisioning runner for the initial administrator account.
 * <p>
 * Security Guidelines:
 * - Credentials are read from secure configuration / environment variables.
 * - Passwords are never logged or stored in plain-text.
 * - Idempotent: restarting the application will not overwrite existing admin passwords or roles.
 * - Fails safely if the configured UID or email conflicts with an existing account.
 */
@Component
@Order(2)
public class AdminProvisioningRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminProvisioningRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.provision.enabled:false}")
    private boolean provisioningEnabled;

    @Value("${admin.provision.uid:}")
    private String adminUid;

    @Value("${admin.provision.name:Campus Administrator}")
    private String adminName;

    @Value("${admin.provision.email:}")
    private String adminEmail;

    @Value("${admin.provision.password:}")
    private String adminPassword;

    public AdminProvisioningRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!provisioningEnabled) {
            log.debug("Admin provisioning is disabled (admin.provision.enabled=false).");
            return;
        }

        if (!StringUtils.hasText(adminUid) || !StringUtils.hasText(adminPassword)) {
            log.warn("Admin provisioning enabled, but UID or password configuration is missing. Skipping.");
            return;
        }

        String normalizedUid = adminUid.trim().toUpperCase();
        String normalizedEmail = StringUtils.hasText(adminEmail) ? adminEmail.trim().toLowerCase() : "";

        // Check if user with this UID already exists (Idempotent / conflict check)
        Optional<User> existingByUid = userRepository.findByCollegeUidIgnoreCase(normalizedUid);
        if (existingByUid.isPresent()) {
            User existingUser = existingByUid.get();
            if (existingUser.getRole() == Role.ADMIN) {
                log.info("Administrator account with UID '{}' already exists with ADMIN role. Provisioning skipped.", normalizedUid);
            } else {
                log.warn("Admin provisioning failed safely: UID '{}' is already in use by a non-administrator account (ID: {}, Role: {}). " +
                        "Will NEVER promote a student account to admin via runner. Provisioning aborted.",
                        normalizedUid, existingUser.getId(), existingUser.getRole());
            }
            return;
        }

        // Check if email conflicts with another existing user
        if (StringUtils.hasText(normalizedEmail)) {
            Optional<User> existingByEmail = userRepository.findByEmail(normalizedEmail);
            if (existingByEmail.isPresent()) {
                User existingUser = existingByEmail.get();
                log.warn("Admin provisioning failed safely: Email '{}' is already in use by an existing account (ID: {}, Role: {}). " +
                        "Will NEVER promote or overwrite an existing account. Provisioning aborted.",
                        normalizedEmail, existingUser.getId(), existingUser.getRole());
                return;
            }
        }

        // Create initial administrator account with ADMIN role and hashed password
        User adminUser = new User(
                normalizedUid,
                adminName.trim(),
                normalizedEmail,
                passwordEncoder.encode(adminPassword),
                Role.ADMIN
        );

        userRepository.save(adminUser);
        log.info("Initial administrator account provisioned successfully (UID: '{}', Email: '{}').",
                normalizedUid, normalizedEmail);
    }
}

