package com.campus.lostfound.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for student registration with credentials.
 */
public class RegisterRequest {

    @NotBlank(message = "College UID is required (e.g. 24BCS10586)")
    @Size(max = 50, message = "College UID must not exceed 50 characters")
    private String collegeUid;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Campus email address is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 12, max = 100, message = "Password must be at least 12 characters in length")
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String collegeUid, String name, String email, String password) {
        this.collegeUid = collegeUid;
        this.name = name;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

