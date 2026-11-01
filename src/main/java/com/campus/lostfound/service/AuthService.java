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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service managing user authentication, registration, password hashing, and token issuance.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new student account.
     * Enforces UID & email uniqueness, server-side STUDENT role assignment,
     * and BCrypt password hashing.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedUid = request.getCollegeUid().trim().toUpperCase();
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String normalizedName = request.getName().trim();

        if (request.getPassword() == null || request.getPassword().length() < 12) {
            throw new IllegalArgumentException("Password must be at least 12 characters in length.");
        }

        // Check if College UID is already in use
        if (userRepository.existsByCollegeUidIgnoreCase(normalizedUid)) {
            throw new DuplicateResourceException("A user with college UID '" + normalizedUid + "' already exists.");
        }

        // Check if Email already exists
        Optional<User> existingEmailUser = userRepository.findByEmail(normalizedEmail);
        if (existingEmailUser.isPresent()) {
            User existing = existingEmailUser.get();
            // Block public self-service account linking for historical uncredentialed accounts
            if (existing.getPasswordHash() == null || existing.getPasswordHash().isEmpty()) {
                throw new BusinessRuleException(
                        "An uncredentialed legacy account with email '" + normalizedEmail + "' exists. " +
                        "To protect historical reports and prevent unauthorized account takeover, " +
                        "self-service email linking is disabled. Please contact a campus administrator to verify and link your account."
                );
            }
            throw new DuplicateResourceException("A user with email '" + normalizedEmail + "' already exists.");
        }

        // New user registration — server strictly assigns STUDENT role
        User userToSave = new User(
                normalizedUid,
                normalizedName,
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                Role.STUDENT
        );

        User savedUser = userRepository.save(userToSave);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                jwtService.getExpirationMs(),
                AuthResponse.UserSummary.fromEntity(savedUser)
        );
    }

    /**
     * Authenticates a user by College UID or Campus Email and password, returning a signed JWT.
     */
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getCollegeUid().trim();

        User user = userRepository.findByCollegeUidIgnoreCase(identifier.toUpperCase())
                .or(() -> userRepository.findByEmail(identifier.toLowerCase()))
                .orElseThrow(() -> new BadCredentialsException("Invalid college UID, email, or password."));

        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            // Historical unlinked account
            throw new BadCredentialsException("Invalid college UID, email, or password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid college UID, email, or password.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                jwtService.getExpirationMs(),
                AuthResponse.UserSummary.fromEntity(user)
        );
    }

    /**
     * Retrieves the current authenticated user's profile derived from the security context.
     */
    public AuthResponse.UserSummary getCurrentUser(UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new BadCredentialsException("User session is unauthenticated or invalid.");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        return AuthResponse.UserSummary.fromEntity(user);
    }

    /**
     * Administrator-assisted legacy account verification and credential setup.
     * Preserves existing user ID, report relationships, and claim history.
     */
    @Transactional
    public UserResponse linkLegacyAccountByAdmin(Long userId, String collegeUid, String rawPassword) {
        if (rawPassword == null || rawPassword.trim().length() < 12) {
            throw new IllegalArgumentException("Password must be at least 12 characters in length.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            throw new BusinessRuleException("User ID " + userId + " already has active credentials. Legacy account linking cannot overwrite existing credentials.");
        }

        String normalizedUid = collegeUid.trim().toUpperCase();
        if (userRepository.existsByCollegeUidIgnoreCase(normalizedUid)) {
            throw new DuplicateResourceException("College UID '" + normalizedUid + "' is already assigned to another account.");
        }

        user.setCollegeUid(normalizedUid);
        user.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        User savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }
}

