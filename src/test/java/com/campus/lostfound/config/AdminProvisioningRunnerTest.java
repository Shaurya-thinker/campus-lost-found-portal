package com.campus.lostfound.config;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.Role;
import com.campus.lostfound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProvisioningRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminProvisioningRunner runner;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(runner, "provisioningEnabled", true);
        ReflectionTestUtils.setField(runner, "adminUid", "ADMIN001");
        ReflectionTestUtils.setField(runner, "adminName", "Campus Admin");
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@campus.edu");
        ReflectionTestUtils.setField(runner, "adminPassword", "SecureAdminPass2026!");
    }

    @Test
    @DisplayName("Should skip provisioning entirely when admin.provision.enabled is false")
    void shouldSkipWhenProvisioningDisabled() {
        ReflectionTestUtils.setField(runner, "provisioningEnabled", false);

        runner.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully provision admin account on first run")
    void shouldProvisionAdminAccountOnFirstRun() {
        when(userRepository.findByCollegeUidIgnoreCase("ADMIN001")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@campus.edu")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("SecureAdminPass2026!")).thenReturn("encoded_admin_pass");

        runner.run(new DefaultApplicationArguments());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should NEVER overwrite password or change existing admin account on restart (Idempotent)")
    void shouldNotOverwriteExistingAdminAccount() {
        User existingAdmin = new User("ADMIN001", "Campus Admin", "admin@campus.edu", "existing_encoded_pass", Role.ADMIN);
        existingAdmin.setId(1L);

        when(userRepository.findByCollegeUidIgnoreCase("ADMIN001")).thenReturn(Optional.of(existingAdmin));

        runner.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Should NEVER promote an existing student account when UID matches configured admin UID (UID Conflict)")
    void shouldFailSafelyAndNeverPromoteStudentOnUidConflict() {
        User studentWithAdminUid = new User("ADMIN001", "Student Pretender", "student@campus.edu", "student_hash", Role.STUDENT);
        studentWithAdminUid.setId(88L);

        when(userRepository.findByCollegeUidIgnoreCase("ADMIN001")).thenReturn(Optional.of(studentWithAdminUid));

        runner.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        // Verify role was not modified
        org.junit.jupiter.api.Assertions.assertEquals(Role.STUDENT, studentWithAdminUid.getRole());
    }

    @Test
    @DisplayName("Should NEVER promote or overwrite an existing user account when email matches configured admin email (Email Conflict)")
    void shouldFailSafelyAndNeverPromoteUserOnEmailConflict() {
        when(userRepository.findByCollegeUidIgnoreCase("ADMIN001")).thenReturn(Optional.empty());

        User existingStudentWithEmail = new User("24BCS99999", "Student Eve", "admin@campus.edu", "eve_hash", Role.STUDENT);
        existingStudentWithEmail.setId(42L);

        when(userRepository.findByEmail("admin@campus.edu")).thenReturn(Optional.of(existingStudentWithEmail));

        runner.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        // Verify role was not modified
        org.junit.jupiter.api.Assertions.assertEquals(Role.STUDENT, existingStudentWithEmail.getRole());
    }
}

