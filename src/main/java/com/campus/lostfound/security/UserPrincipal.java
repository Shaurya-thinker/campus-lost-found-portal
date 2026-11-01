package com.campus.lostfound.security;

import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails implementation representing an authenticated campus user.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String collegeUid;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserPrincipal(Long id, String collegeUid, String name, String email,
                         String password, Role role,
                         Collection<? extends GrantedAuthority> authorities,
                         boolean enabled) {
        this.id = id;
        this.collegeUid = collegeUid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public UserPrincipal(Long id, String collegeUid, String name, String email,
                         String password, Role role,
                         Collection<? extends GrantedAuthority> authorities) {
        this(id, collegeUid, name, email, password, role, authorities, true);
    }

    public static UserPrincipal create(User user) {
        Role role = user.getRole() != null ? user.getRole() : Role.STUDENT;
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );

        boolean active = user.isEnabled() && user.getPasswordHash() != null && !user.getPasswordHash().isEmpty();

        return new UserPrincipal(
                user.getId(),
                user.getCollegeUid(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                role,
                authorities,
                active
        );
    }

    public Long getId() {
        return id;
    }

    public String getCollegeUid() {
        return collegeUid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return collegeUid != null ? collegeUid : email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

