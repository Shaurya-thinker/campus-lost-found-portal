package com.campus.lostfound.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for user login using College UID and password.
 */
public class LoginRequest {

    @NotBlank(message = "College UID is required")
    private String collegeUid;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String collegeUid, String password) {
        this.collegeUid = collegeUid;
        this.password = password;
    }

    public String getCollegeUid() {
        return collegeUid;
    }

    public void setCollegeUid(String collegeUid) {
        this.collegeUid = collegeUid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

