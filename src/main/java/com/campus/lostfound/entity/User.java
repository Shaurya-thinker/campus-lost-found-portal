package com.campus.lostfound.entity;

import com.campus.lostfound.entity.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a student or campus member who reports items or submits claims.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "college_uid", unique = true, length = 50)
    private String collegeUid;

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.STUDENT;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    // Default no-argument constructor required by JPA
    public User() {
    }

    // Parameterized constructor for creating legacy/basic users
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.role = Role.STUDENT;
    }

    // Full constructor for basic user
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = Role.STUDENT;
    }

    // Parameterized constructor for authenticated student registration
    public User(String collegeUid, String name, String email, String passwordHash, Role role) {
        this.collegeUid = collegeUid;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : Role.STUDENT;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCollegeUid() {
        return collegeUid;
    }

    public void setCollegeUid(String collegeUid) {
        this.collegeUid = collegeUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

