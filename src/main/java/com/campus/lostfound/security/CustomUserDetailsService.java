package com.campus.lostfound.security;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads user details by College UID or Email for authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("User identifier cannot be empty.");
        }

        String normalized = identifier.trim();

        // Try lookup by College UID first
        User user = userRepository.findByCollegeUidIgnoreCase(normalized)
                .or(() -> userRepository.findByEmail(normalized.toLowerCase()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + normalized));

        return UserPrincipal.create(user);
    }

    /**
     * Loads user details by primary key ID.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        if (id == null) {
            throw new UsernameNotFoundException("User ID cannot be null.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }
}

