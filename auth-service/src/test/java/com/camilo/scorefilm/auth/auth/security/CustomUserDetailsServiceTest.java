package com.camilo.scorefilm.auth.auth.security;

import com.camilo.scorefilm.auth.user.entity.User;
import com.camilo.scorefilm.auth.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks CustomUserDetailsService service;

    @Test
    void loadUserByUsername_existingUser_returnsSecurityDetails() {
        User user = User.builder().username("camilo").passwordHash("hash").enabled(true).build();
        when(userRepository.findByUsernameOrEmail("camilo")).thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("camilo");

        assertThat(result.getUsername()).isEqualTo("camilo");
        assertThat(result.isEnabled()).isTrue();
        verify(userRepository).findByUsernameOrEmail("camilo");
    }

    @Test
    void loadUserByUsername_unknownUser_throwsNotFound() {
        when(userRepository.findByUsernameOrEmail("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
