package com.campus.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for administrator-assisted legacy account credential setup.
 */
public class LegacyAccountLinkRequest {

    @NotBlank(message = "College UID is required")
    @Size(min = 3, max = 50, message = "College UID must be between 3 and 50 characters")
    private String collegeUid;

    @NotBlank(message = "Password is required")
    @Size(min = 12, message = "Password must be at least 12 characters in length")
    private String password;

    public LegacyAccountLinkRequest() {
    }

    public LegacyAccountLinkRequest(String collegeUid, String password) {
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

