package com.camilo.scorefilm.auth.user.service;

import com.camilo.scorefilm.auth.role.entity.Role;
import com.camilo.scorefilm.auth.user.dto.UserResponse;
import com.camilo.scorefilm.auth.user.entity.User;
import com.camilo.scorefilm.auth.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("should map a found user into a response DTO with roles")
    void getUserByUsername_userFound_mapsRolesAndFields() {
        // Arrange
        User user = userWithRoles(
                "camilo",
                "Camilo",
                "Perez",
                "camilo@example.com",
                Set.of(role("USER"), role("ADMIN"))
        );
        when(userRepository.findByUsername("camilo")).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUserByUsername("camilo");

        // Assert
        assertThat(response.getUsername()).isEqualTo("camilo");
        assertThat(response.getFirstName()).isEqualTo("Camilo");
        assertThat(response.getLastName()).isEqualTo("Perez");
        assertThat(response.getEmail()).isEqualTo("camilo@example.com");
        assertThat(response.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("should throw when the user does not exist")
    void getUserByUsername_userNotFound_throwsException() {
        // Arrange
        when(userRepository.findByUsername("camilo")).thenReturn(Optional.empty());

        // Act
        RuntimeException exception = catchThrowableOfType(
                RuntimeException.class,
                () -> userService.getUserByUsername("camilo")
        );

        // Assert
        assertThat(exception)
                .withFailMessage("Expected getUserByUsername() to fail when no user is found for username '%s'", "camilo")
                .hasMessageContaining("Usuario no encontrado");
    }

    private User userWithRoles(
            String username,
            String firstName,
            String lastName,
            String email,
            Set<Role> roles
    ) {
        return User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .passwordHash("hashed-password")
                .enabled(true)
                .roles(new HashSet<>(roles))
                .build();
    }

    private Role role(String name) {
        return Role.builder()
                .name(name)
                .build();
    }
}
