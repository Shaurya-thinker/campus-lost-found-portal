package com.campus.lostfound.dto;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.Role;

/**
 * Response payload returned after successful registration or authentication.
 */
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserSummary user;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiresIn, UserSummary user) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserSummary getUser() {
        return user;
    }

    public void setUser(UserSummary user) {
        this.user = user;
    }

    public static class UserSummary {
        private Long id;
        private String collegeUid;
        private String name;
        private String email;
        private Role role;

        public UserSummary() {
        }

        public UserSummary(Long id, String collegeUid, String name, String email, Role role) {
            this.id = id;
            this.collegeUid = collegeUid;
            this.name = name;
            this.email = email;
            this.role = role;
        }

        public static UserSummary fromEntity(User user) {
            if (user == null) {
                return null;
            }
            return new UserSummary(
                    user.getId(),
                    user.getCollegeUid(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            );
        }

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

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }
    }
}

