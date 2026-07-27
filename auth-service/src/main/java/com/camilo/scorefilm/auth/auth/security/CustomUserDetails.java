package com.camilo.scorefilm.auth.auth.security;

import com.camilo.scorefilm.auth.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Boolean enabled;
    private final Set<GrantedAuthority> authorities;

    private CustomUserDetails(
            String username,
            String password,
            Boolean enabled,
            Set<GrantedAuthority> authorities
    ) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static CustomUserDetails from(User user) {

        Set<GrantedAuthority> authorities = new HashSet<>();
        
        for (var role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(toAuthority(role.getName())));
        }

        return new CustomUserDetails(
                user.getUsername(),
                user.getPasswordHash(),
                user.getEnabled(),
                authorities
        );
    }

    private static String toAuthority(String roleName) {
        String normalizedRole = roleName.trim().toUpperCase(Locale.ROOT);
        return normalizedRole.startsWith("ROLE_")
                ? normalizedRole
                : "ROLE_" + normalizedRole;
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
        return username;
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
