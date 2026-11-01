package com.campus.lostfound.service;

import com.campus.lostfound.dto.AuthResponse;
import com.campus.lostfound.dto.LoginRequest;
import com.campus.lostfound.dto.RegisterRequest;
import com.campus.lostfound.dto.UserResponse;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.Role;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.exception.DuplicateResourceException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.UserRepository;
import com.campus.lostfound.security.JwtService;
import com.campus.lostfound.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User studentUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("24BCS10586", "Dave Miller", "dave.miller@campus.edu", "StrongPassword123!");
        loginRequest = new LoginRequest("24BCS10586", "StrongPassword123!");

        studentUser = new User("24BCS10586", "Dave Miller", "dave.miller@campus.edu", "hashed_password", Role.STUDENT);
        studentUser.setId(10L);
    }

    @Test
    @DisplayName("Should successfully register a new student with server-side STUDENT role and hashed password")
    void shouldRegisterStudentSuccessfully() {
        when(userRepository.existsByCollegeUidIgnoreCase("24BCS10586")).thenReturn(false);
        when(userRepository.findByEmail("dave.miller@campus.edu")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPassword123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(studentUser);
        when(jwtService.generateToken(studentUser)).thenReturn("sample.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(1800000L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("sample.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(Role.STUDENT, response.getUser().getRole());
        assertEquals("24BCS10586", response.getUser().getCollegeUid());
        verify(passwordEncoder).encode("StrongPassword123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when College UID already exists (409 Conflict)")
    void shouldRejectDuplicateCollegeUid() {
        when(userRepository.existsByCollegeUidIgnoreCase("24BCS10586")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("Should reject registration when email already exists for an account with credentials")
    void shouldRejectDuplicateEmailWhenAccountHasCredentials() {
        when(userRepository.existsByCollegeUidIgnoreCase("24BCS10586")).thenReturn(false);
        User existingCredentialedUser = new User("99BCS99999", "Other User", "dave.miller@campus.edu", "already_hashed", Role.STUDENT);
        when(userRepository.findByEmail("dave.miller@campus.edu")).thenReturn(Optional.of(existingCredentialedUser));

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("Should reject public self-service registration when email matches an uncredentialed legacy account")
    void shouldRejectPublicSelfServiceRegistrationForLegacyAccount() {
        when(userRepository.existsByCollegeUidIgnoreCase("24BCS10586")).thenReturn(false);
        User legacyUser = new User("Dave Miller", "dave.miller@campus.edu");
        legacyUser.setId(5L); // Has legacy history but no credentials
        legacyUser.setPasswordHash(null);

        when(userRepository.findByEmail("dave.miller@campus.edu")).thenReturn(Optional.of(legacyUser));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authService.register(registerRequest));
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("self-service email linking is disabled"));
    }

    @Test
    @DisplayName("Should successfully link credentials to legacy account via administrator-assisted process")
    void shouldLinkLegacyAccountViaAdminProcess() {
        User legacyUser = new User("Dave Miller", "dave.miller@campus.edu");
        legacyUser.setId(5L);
        legacyUser.setPasswordHash(null);

        when(userRepository.findById(5L)).thenReturn(Optional.of(legacyUser));
        when(userRepository.existsByCollegeUidIgnoreCase("24BCS10586")).thenReturn(false);
        when(passwordEncoder.encode("StrongPassword123!")).thenReturn("hashed_password");
        when(userRepository.save(legacyUser)).thenReturn(legacyUser);

        UserResponse response = authService.linkLegacyAccountByAdmin(5L, "24BCS10586", "StrongPassword123!");

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("24BCS10586", legacyUser.getCollegeUid());
        assertEquals("hashed_password", legacyUser.getPasswordHash());
        assertEquals(Role.STUDENT, legacyUser.getRole());
        org.junit.jupiter.api.Assertions.assertTrue(legacyUser.isEnabled());
    }

    @Test
    @DisplayName("Should reject admin legacy linking if user already has credentials")
    void shouldRejectAdminLegacyLinkingWhenUserAlreadyHasCredentials() {
        User existingUser = new User("24BCS10586", "Dave Miller", "dave.miller@campus.edu", "existing_hash", Role.STUDENT);
        existingUser.setId(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(existingUser));

        assertThrows(BusinessRuleException.class,
                () -> authService.linkLegacyAccountByAdmin(5L, "24BCS10586", "StrongPassword123!"));
    }

    @Test
    @DisplayName("Should reject admin legacy linking if college UID conflicts with another account")
    void shouldRejectAdminLegacyLinkingWhenUidConflicts() {
        User legacyUser = new User("Dave Miller", "dave.miller@campus.edu");
        legacyUser.setId(5L);
        legacyUser.setPasswordHash(null);

        when(userRepository.findById(5L)).thenReturn(Optional.of(legacyUser));
        when(userRepository.existsByCollegeUidIgnoreCase("24BCS10586")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.linkLegacyAccountByAdmin(5L, "24BCS10586", "StrongPassword123!"));
    }

    @Test
    @DisplayName("Should successfully log in with valid UID and password")
    void shouldLoginSuccessfully() {
        when(userRepository.findByCollegeUidIgnoreCase("24BCS10586")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("StrongPassword123!", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(studentUser)).thenReturn("login.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(1800000L);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("login.jwt.token", response.getToken());
        assertEquals("24BCS10586", response.getUser().getCollegeUid());
    }

    @Test
    @DisplayName("Should successfully log in with valid email and password")
    void shouldLoginSuccessfullyWithEmail() {
        when(userRepository.findByCollegeUidIgnoreCase("DAVE.MILLER@CAMPUS.EDU")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("dave.miller@campus.edu")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("StrongPassword123!", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(studentUser)).thenReturn("login.jwt.token.email");
        when(jwtService.getExpirationMs()).thenReturn(1800000L);

        LoginRequest emailLogin = new LoginRequest("dave.miller@campus.edu", "StrongPassword123!");
        AuthResponse response = authService.login(emailLogin);

        assertNotNull(response);
        assertEquals("login.jwt.token.email", response.getToken());
        assertEquals("24BCS10586", response.getUser().getCollegeUid());
    }

    @Test
    @DisplayName("Should reject login with invalid password using generic credentials error")
    void shouldRejectLoginWithInvalidPassword() {
        when(userRepository.findByCollegeUidIgnoreCase("24BCS10586")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("WrongPassword", "hashed_password")).thenReturn(false);

        LoginRequest badRequest = new LoginRequest("24BCS10586", "WrongPassword");
        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> authService.login(badRequest));
        assertEquals("Invalid college UID, email, or password.", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject login with unknown College UID using generic credentials error")
    void shouldRejectLoginWithUnknownUid() {
        when(userRepository.findByCollegeUidIgnoreCase("UNKNOWN_UID")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown_uid")).thenReturn(Optional.empty());

        LoginRequest unknownRequest = new LoginRequest("UNKNOWN_UID", "AnyPassword123!");
        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> authService.login(unknownRequest));
        assertEquals("Invalid college UID, email, or password.", ex.getMessage());
    }

    @Test
    @DisplayName("Should retrieve current authenticated user details via /api/auth/me")
    void shouldReturnCurrentUserFromSecurityContext() {
        UserPrincipal principal = UserPrincipal.create(studentUser);
        when(userRepository.findById(10L)).thenReturn(Optional.of(studentUser));

        AuthResponse.UserSummary summary = authService.getCurrentUser(principal);

        assertNotNull(summary);
        assertEquals(10L, summary.getId());
        assertEquals("24BCS10586", summary.getCollegeUid());
        assertEquals(Role.STUDENT, summary.getRole());
    }
}

