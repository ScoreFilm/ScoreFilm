package com.camilo.scorefilm.auth.auth.service;

import com.camilo.scorefilm.auth.auth.dto.AuthResponse;
import com.camilo.scorefilm.auth.auth.dto.LoginRequest;
import com.camilo.scorefilm.auth.auth.dto.RegisterRequest;
import com.camilo.scorefilm.auth.auth.security.JwtService;
import com.camilo.scorefilm.auth.role.entity.Role;
import com.camilo.scorefilm.auth.role.repository.RoleRepository;
import com.camilo.scorefilm.auth.user.entity.User;
import com.camilo.scorefilm.auth.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void registerCreatesUserWithEncodedPasswordAndReturnsToken() {
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

        AuthResponse response = authService.register(request);

        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(userRepository).save(userCaptor.capture());
        verify(jwtService).generateToken(userDetailsCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
        assertThat(userCaptor.getValue().getPasswordHash()).isNotEqualTo(RAW_PASSWORD);
        assertThat(userCaptor.getValue().getRoles()).containsExactly(userRole);
        assertThat(userDetailsCaptor.getValue().getUsername()).isEqualTo(request.getUsername());
        assertThat(userDetailsCaptor.getValue().getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(response.getAccessToken()).isEqualTo(TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo(request.getUsername());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = registerRequest();

        when(userRepository.existsUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya está registrado");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        RegisterRequest request = registerRequest();

        when(userRepository.existsUsername(request.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya existe");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

        @Test
        void registerPropagatesJwtGenerationFailureAfterSavingUser() {
                RegisterRequest request = registerRequest();
                Role userRole = userRole();
                User savedUser = userWithRole(ENCODED_PASSWORD, userRole);
                RuntimeException tokenFailure = new RuntimeException("RSA private key unavailable");

                when(userRepository.existsUsername(request.getUsername())).thenReturn(false);
                when(userRepository.existsEmail(request.getEmail())).thenReturn(false);
                when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
                when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
                when(userRepository.save(any(User.class))).thenReturn(savedUser);
                when(jwtService.generateToken(any(UserDetails.class))).thenThrow(tokenFailure);

                // El usuario ya se persiste antes de firmar el token; este test documenta ese comportamiento actual.
                assertThatThrownBy(() -> authService.register(request))
                                .isSameAs(tokenFailure);

                InOrder inOrder = inOrder(userRepository, jwtService);
                inOrder.verify(userRepository).save(any(User.class));
                inOrder.verify(jwtService).generateToken(any(UserDetails.class));
        }

    @Test
    void loginAuthenticatesUserAndReturnsToken() {
        LoginRequest request = loginRequest();
        User user = userWithRole(ENCODED_PASSWORD, userRole());
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        ArgumentCaptor<UserDetails> userDetailsCaptor = ArgumentCaptor.forClass(UserDetails.class);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(TOKEN);

        AuthResponse response = authService.login(request);

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
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void loginThrowsBadCredentialsWhenPasswordIsIncorrect() {
        LoginRequest request = loginRequest();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(userWithRole(ENCODED_PASSWORD, userRole())));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Email o contraseña incorrectos");

        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void loginThrowsBadCredentialsWhenUserIsNotFound() {
        LoginRequest request = loginRequest();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Email o contraseña incorrectos");

        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

        @Test
        void loginPropagatesJwtGenerationFailureWithoutSavingUser() {
                LoginRequest request = loginRequest();
                User user = userWithRole(ENCODED_PASSWORD, userRole());
                RuntimeException tokenFailure = new RuntimeException("RSA private key unavailable");

                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
                when(jwtService.generateToken(any(UserDetails.class))).thenThrow(tokenFailure);

                assertThatThrownBy(() -> authService.login(request))
                                .isSameAs(tokenFailure);

                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
                verify(userRepository, never()).save(any(User.class));
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
