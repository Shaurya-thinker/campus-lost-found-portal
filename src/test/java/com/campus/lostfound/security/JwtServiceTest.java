package com.campus.lostfound.security;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "TestSecretKeyForTestingJwtTokenGenerationAndValidation2026!";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 1800000L); // 30 minutes
        jwtService.init();
    }

    @Test
    @DisplayName("Should generate valid JWT token with user claims and verify successfully")
    void shouldGenerateAndValidateToken() {
        User user = new User("24BCS10586", "John Doe", "john.doe@campus.edu", "hashed_pwd", Role.STUDENT);
        user.setId(42L);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("24BCS10586", jwtService.extractSubject(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("STUDENT", jwtService.extractRole(token));
    }

    @Test
    @DisplayName("Should reject malformed or tampered token")
    void shouldRejectMalformedToken() {
        assertFalse(jwtService.validateToken("invalid.token.structure"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        // Setup service with negative expiration (already expired)
        JwtService expiredService = new JwtService();
        ReflectionTestUtils.setField(expiredService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredService, "jwtExpirationMs", -1000L);
        expiredService.init();

        User user = new User("24BCS10586", "John Doe", "john.doe@campus.edu", "hashed_pwd", Role.STUDENT);
        user.setId(42L);

        String expiredToken = expiredService.generateToken(user);
        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    @DisplayName("UserPrincipal should be enabled for active users with password hash")
    void shouldBeEnabledForActiveCredentialedUser() {
        User activeUser = new User("24BCS10586", "Active Student", "active@campus.edu", "valid_hash", Role.STUDENT);
        activeUser.setEnabled(true);

        UserPrincipal principal = UserPrincipal.create(activeUser);
        assertTrue(principal.isEnabled());
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    @DisplayName("UserPrincipal should be DISABLED when account enabled flag is false")
    void shouldBeDisabledWhenUserFlagIsFalse() {
        User disabledUser = new User("24BCS10586", "Disabled Student", "disabled@campus.edu", "valid_hash", Role.STUDENT);
        disabledUser.setEnabled(false);

        UserPrincipal principal = UserPrincipal.create(disabledUser);
        assertFalse(principal.isEnabled());
    }

    @Test
    @DisplayName("UserPrincipal should be DISABLED when user has no password hash (uncredentialed legacy account)")
    void shouldBeDisabledWhenPasswordHashIsNull() {
        User uncredentialedUser = new User("Legacy Student", "legacy@campus.edu");
        uncredentialedUser.setPasswordHash(null);

        UserPrincipal principal = UserPrincipal.create(uncredentialedUser);
        assertFalse(principal.isEnabled());
    }

    @Test
    @DisplayName("Changing user role in database immediately reflects in UserPrincipal authorities, neutralizing stale tokens")
    void shouldDeriveAuthoritiesFromCurrentDatabaseEntityNotStaleToken() {
        // User had an ADMIN token issued historically
        User userInitiallyAdmin = new User("24BCS10586", "User", "user@campus.edu", "hash", Role.ADMIN);
        userInitiallyAdmin.setId(50L);
        String staleAdminToken = jwtService.generateToken(userInitiallyAdmin);

        assertEquals("ADMIN", jwtService.extractRole(staleAdminToken));

        // In the database, the user's role has been changed/demoted to STUDENT
        User userNowStudent = new User("24BCS10586", "User", "user@campus.edu", "hash", Role.STUDENT);
        userNowStudent.setId(50L);

        // Principal recreated from current database entity
        UserPrincipal updatedPrincipal = UserPrincipal.create(userNowStudent);

        // Authorities reflect current database role, NOT the stale token claim
        assertEquals(Role.STUDENT, updatedPrincipal.getRole());
        assertTrue(updatedPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
        assertFalse(updatedPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}

