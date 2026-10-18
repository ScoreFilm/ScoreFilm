package com.camilo.scorefilm.auth.auth.security;

import com.camilo.scorefilm.auth.role.entity.Role;
import com.camilo.scorefilm.auth.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void from_rolesWithAndWithoutPrefix_normalizesAuthoritiesAndFields() {
        User user = User.builder()
                .username("camilo")
                .passwordHash("hash")
                .enabled(false)
                .roles(new HashSet<>(Set.of(Role.builder().name(" user ").build(),
                        Role.builder().name("ROLE_ADMIN").build())))
                .build();

        CustomUserDetails details = CustomUserDetails.from(user);

        assertThat(details.getUsername()).isEqualTo("camilo");
        assertThat(details.getPassword()).isEqualTo("hash");
        assertThat(details.isEnabled()).isFalse();
        assertThat(details.getAuthorities()).extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }
}
