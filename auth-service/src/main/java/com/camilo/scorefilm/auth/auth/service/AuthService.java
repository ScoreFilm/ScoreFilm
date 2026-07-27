package com.camilo.scorefilm.auth.auth.service;

import com.camilo.scorefilm.auth.auth.dto.AuthResponse;
import com.camilo.scorefilm.auth.auth.dto.LoginRequest;
import com.camilo.scorefilm.auth.auth.dto.RegisterRequest;
import com.camilo.scorefilm.auth.auth.security.CustomUserDetails;
import com.camilo.scorefilm.auth.auth.security.JwtService;
import com.camilo.scorefilm.auth.role.entity.Role;
import com.camilo.scorefilm.auth.role.repository.RoleRepository;
import com.camilo.scorefilm.auth.user.entity.User;
import com.camilo.scorefilm.auth.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getUsername());

        // PASO 1: Validar que no exista
        if (userRepository.existsUsername(request.getUsername())) {
            throw new RuntimeException("El usuario '" + request.getUsername() + "' ya existe");
        }

        if (userRepository.existsEmail(request.getEmail())) {
            throw new RuntimeException("El email '" + request.getEmail() + "' ya está registrado");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> 
                    new RuntimeException("Rol 'USER' no encontrado en BD. Crea los roles primero.")
                );

        User newUser = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Usuario registrado exitosamente: {}", savedUser.getUsername());

        CustomUserDetails userDetails = CustomUserDetails.from(savedUser);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(900000)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt para email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> 
                    new BadCredentialsException("Email o contraseña incorrectos")
                );

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            
            log.info("Autenticación exitosa para: {}", request.getEmail());

        } catch (AuthenticationException e) {
            log.warn("Fallo de autenticación para: {}", request.getEmail());
            throw new BadCredentialsException("Email o contraseña incorrectos", e);
        }

        CustomUserDetails userDetails = CustomUserDetails.from(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(900000)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
