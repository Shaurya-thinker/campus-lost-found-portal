package com.campus.lostfound.controller;

import com.campus.lostfound.dto.UserRegistrationRequest;
import com.campus.lostfound.dto.UserResponse;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller managing user registration and profile lookup endpoints.
 * Base path: /api/users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user.
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        User user = new User(request.getName(), request.getEmail());
        User savedUser = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(savedUser));
    }

    /**
     * Retrieves a user by their unique primary key ID.
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Retrieves a user by their email address via query parameter.
     * GET /api/users?email=student@campus.edu
     */
    @GetMapping
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam(required = false) String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessRuleException("The 'email' query parameter is required when querying /api/users without a path ID.");
        }
        User user = userService.getUserByEmail(email.trim());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}

