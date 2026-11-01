package com.campus.lostfound.service;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.exception.DuplicateResourceException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user management and registration business logic.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user after verifying that the email address is unique.
     *
     * @param user user entity to register
     * @return persisted user
     * @throws DuplicateResourceException if the email already exists
     */
    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + user.getEmail() + "' already exists.");
        }
        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their primary key ID.
     *
     * @param id user ID
     * @return User entity
     * @throws ResourceNotFoundException if user is not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Retrieves a user by their unique email address.
     *
     * @param email user email
     * @return User entity
     * @throws ResourceNotFoundException if user is not found
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}

