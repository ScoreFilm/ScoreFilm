package com.camilo.scorefilm.auth.config;

import com.camilo.scorefilm.auth.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        http.cors(Customizer.withDefaults());
        
        // API stateless autenticada solo por JWT en el header Authorization (sin cookies
        // ni sesiones): no hay vector CSRF, por eso se deshabilita.
        http.csrf(csrf -> csrf.disable());
        
        // Stateless: sin sesiones HTTP, solo JWT
        http.sessionManagement(session -> session.sessionCreationPolicy(
            org.springframework.security.config.http.SessionCreationPolicy.STATELESS
        ));
        
        /*
         * CONFIGURACIÓN DE AUTORIZACIÓN
         * 
         * El orden IMPORTA. Las rutas se evalúan en orden:
         * 1. Si coincide la primera, se aplica esa regla
         * 2. Si no coincide, continúa a la siguiente
         * 3. Si ninguna coincide, se aplica anyRequest()
         * 
         * Por eso las rutas MÁS ESPECÍFICAS van primero
         */
        http.authorizeHttpRequests(authorize -> {
            authorize
                // Rutas públicas (sin autenticación)
                .requestMatchers("/api/auth/**").permitAll()  // Login y register
                .requestMatchers("/.well-known/**").permitAll() // Public JWKS
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll() // Swagger UI and OpenAPI specification
                
                // Rutas protegidas (requieren autenticación)
                .requestMatchers("/api/users/me").authenticated()  // Datos del usuario actual
                
                // Cualquier otra ruta necesita un JWT válido.
                .anyRequest().authenticated();
        });

        /*
         * REGISTRAR JWT FILTER
         * 
         * .addFilterBefore() inserta el filtro ANTES de otro.
         * 
         * UsernamePasswordAuthenticationFilter es el filtro que procesa
         * login/password tradicionales (no lo usamos aquí, pero lo usamos
         * como referencia de "posición")
         * 
         * FLUJO DE FILTROS:
         * 
         * 1. JwtAuthenticationFilter (nuestro filtro)
         *    ├─ Extrae JWT del header
         *    ├─ Valida y carga usuario
         *    └─ Setea Authentication en SecurityContext
         *    
         * 2. UsernamePasswordAuthenticationFilter
         *    (normalmente procesa login form, no lo usamos)
         *    
         * 3. ... otros filtros de Spring ...
         * 
         * 4. AuthorizationFilter
         *    ├─ Verifica permisos basado en @RequestMatchers de arriba
         *    ├─ Si /api/auth/** → permitAll (no necesita autenticación)
         *    ├─ Si /api/users/me → authenticated (necesita estar en SecurityContext)
         *    └─ Si no está autenticado y lo requiere → 401 Unauthorized
         * 
         * 5. Controlador
         */
        http.addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        /*
         * Spring ya crea un AuthenticationManager automáticamente.
         * 
         * authenticationConfiguration.getAuthenticationManager()
         * obtiene el que Spring creó.
         */
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService
    ) {
        /*
         * En Spring Security 6+, DaoAuthenticationProvider requiere
         * UserDetailsService en el constructor.
         */
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
