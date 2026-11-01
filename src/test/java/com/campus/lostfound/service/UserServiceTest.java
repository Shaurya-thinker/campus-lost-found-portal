package com.campus.lostfound.service;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.exception.DuplicateResourceException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Alice Smith", "alice.smith@campus.edu");
    }

    @Test
    @DisplayName("Should successfully register a new user with unique email")
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail("alice.smith@campus.edu")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User registered = userService.registerUser(sampleUser);

        assertNotNull(registered);
        assertEquals("Alice Smith", registered.getName());
        assertEquals("alice.smith@campus.edu", registered.getEmail());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void testRegisterUser_DuplicateEmail() {
        when(userRepository.existsByEmail("alice.smith@campus.edu")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(sampleUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should return user when found by ID")
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        User found = userService.getUserById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found by ID")
    void testGetUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }
}

