package com.campus.lostfound.repository;

import com.campus.lostfound.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Checks if a user already exists with the given email address.
     *
     * @param email user email address to verify
     * @return true if an existing user has this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Retrieves a user by their unique email address.
     *
     * @param email user email address
     * @return Optional containing the User if found, or empty
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given college UID (case-insensitive).
     *
     * @param collegeUid student college UID
     * @return true if an existing user has this UID, false otherwise
     */
    boolean existsByCollegeUidIgnoreCase(String collegeUid);

    /**
     * Retrieves a user by their college UID (case-insensitive).
     *
     * @param collegeUid student college UID
     * @return Optional containing the User if found, or empty
     */
    Optional<User> findByCollegeUidIgnoreCase(String collegeUid);
}

