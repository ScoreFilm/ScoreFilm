package com.camilo.scorefilm.auth.auth.service;

import com.camilo.scorefilm.auth.auth.dto.AuthResponse;
import com.camilo.scorefilm.auth.auth.dto.LoginRequest;
import com.camilo.scorefilm.auth.auth.dto.RegisterRequest;
import com.camilo.scorefilm.auth.auth.security.JwtService;
import com.camilo.scorefilm.auth.role.entity.Role;
import com.camilo.scorefilm.auth.role.repository.RoleRepository;
import com.camilo.scorefilm.auth.user.entity.User;
import com.camilo.scorefilm.auth.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    private static final String RAW_PASSWORD = "Password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded-password";
    private static final String TOKEN = "jwt-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("should create a user, encode the password and return a bearer token")
        void register_validUser_returnsAuthResponse() {
            // Arrange
            RegisterRequest request = registerRequest();
            Role userRole = userRole();
            User savedUser = userWithRole(ENCODED_PASSWORD, userRole);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<UserDetails> userDetailsCaptor = ArgumentCaptor.forClass(UserDetails.class);

            when(userRepository.existsUsername(request.getUsername())).thenReturn(false);
            when(userRepository.existsEmail(request.getEmail())).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtService.generateToken(any(UserDetails.class))).thenReturn(TOKEN);

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            verify(passwordEncoder).encode(RAW_PASSWORD);
            verify(userRepository).save(userCaptor.capture());
            verify(jwtService).generateToken(userDetailsCaptor.capture());

            assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
            assertThat(userCaptor.getValue().getRoles()).containsExactly(userRole);
            assertThat(userDetailsCaptor.getValue().getUsername()).isEqualTo(request.getUsername());
            assertThat(userDetailsCaptor.getValue().getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_USER");
            assertThat(response.getAccessToken()).isEqualTo(TOKEN);
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(900000);
            assertThat(response.getUsername()).isEqualTo(request.getUsername());
            assertThat(response.getEmail()).isEqualTo(request.getEmail());
        }

        @Test
        @DisplayName("should reject registration when the email is already registered")
        void register_duplicateEmail_throwsRuntimeException() {
            // Arrange
            RegisterRequest request = registerRequest();

            when(userRepository.existsUsername(request.getUsername())).thenReturn(false);
            when(userRepository.existsEmail(request.getEmail())).thenReturn(true);

            // Act
            RuntimeException exception = catchThrowableOfType(
                    RuntimeException.class,
                    () -> authService.register(request)
            );

            // Assert
            assertThat(exception)
                    .withFailMessage("Expected register() to reject duplicate email '%s'", request.getEmail())
                    .hasMessageContaining("El email '" + request.getEmail() + "'")
                    .hasMessageContaining("registrado");

            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(any());
            verify(jwtService, never()).generateToken(any(UserDetails.class));
        }

        @Test
        @DisplayName("should reject registration when the username already exists")
        void register_duplicateUsername_throwsRuntimeException() {
            // Arrange
            RegisterRequest request = registerRequest();

            when(userRepository.existsUsername(request.getUsername())).thenReturn(true);

            // Act
            RuntimeException exception = catchThrowableOfType(
                    RuntimeException.class,
                    () -> authService.register(request)
            );

            // Assert
            assertThat(exception)
                    .withFailMessage("Expected register() to reject duplicate username '%s'", request.getUsername())
                    .hasMessageContaining("El usuario '" + request.getUsername() + "'")
                    .hasMessageContaining("ya existe");

            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(any());
            verify(jwtService, never()).generateToken(any(UserDetails.class));
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should authenticate the user and return a bearer token")
        void login_validCredentials_returnsAuthResponse() {
            // Arrange
            LoginRequest request = loginRequest();
            User user = userWithRole(ENCODED_PASSWORD, userRole());
            ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            ArgumentCaptor<UserDetails> userDetailsCaptor = ArgumentCaptor.forClass(UserDetails.class);

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            when(jwtService.generateToken(any(UserDetails.class))).thenReturn(TOKEN);

            // Act
            AuthResponse response = authService.login(request);

            // Assert
            verify(authenticationManager).authenticate(authenticationCaptor.capture());
            verify(jwtService).generateToken(userDetailsCaptor.capture());

            assertThat(authenticationCaptor.getValue().getPrincipal()).isEqualTo(request.getEmail());
            assertThat(authenticationCaptor.getValue().getCredentials()).isEqualTo(request.getPassword());
            assertThat(userDetailsCaptor.getValue().getUsername()).isEqualTo(user.getUsername());
            assertThat(userDetailsCaptor.getValue().getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_USER");
            assertThat(response.getAccessToken()).isEqualTo(TOKEN);
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(900000);
            assertThat(response.getUsername()).isEqualTo(user.getUsername());
            assertThat(response.getEmail()).isEqualTo(user.getEmail());
        }

        @Test
        @DisplayName("should reject login when no user exists for the email")
        void login_unknownUser_throwsBadCredentialsException() {
            // Arrange
            LoginRequest request = loginRequest();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            // Act
            BadCredentialsException exception = catchThrowableOfType(
                    BadCredentialsException.class,
                    () -> authService.login(request)
            );

            // Assert
            assertThat(exception)
                    .withFailMessage("Expected login() to reject unknown email '%s'", request.getEmail())
                    .hasMessageContaining("Email o contrase");

            verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtService, never()).generateToken(any(UserDetails.class));
        }

        @Test
        @DisplayName("should reject login when Spring Security cannot authenticate the credentials")
        void login_invalidCredentials_throwsBadCredentialsException() {
            // Arrange
            LoginRequest request = loginRequest();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(userWithRole(ENCODED_PASSWORD, userRole())));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act
            BadCredentialsException exception = catchThrowableOfType(
                    BadCredentialsException.class,
                    () -> authService.login(request)
            );

            // Assert
            assertThat(exception)
                    .withFailMessage("Expected login() to reject invalid credentials for email '%s'", request.getEmail())
                    .hasMessageContaining("Email o contrase");

            verify(jwtService, never()).generateToken(any(UserDetails.class));
        }
    }

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .username("camilo")
                .firstName("Camilo")
                .lastName("Perez")
                .email("camilo@example.com")
                .password(RAW_PASSWORD)
                .build();
    }

    private LoginRequest loginRequest() {
        return LoginRequest.builder()
                .email("camilo@example.com")
                .password(RAW_PASSWORD)
                .build();
    }

    private Role userRole() {
        return Role.builder()
                .name("USER")
                .build();
    }

    private User userWithRole(String passwordHash, Role role) {
        return User.builder()
                .username("camilo")
                .firstName("Camilo")
                .lastName("Perez")
                .email("camilo@example.com")
                .passwordHash(passwordHash)
                .enabled(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
    }
}
