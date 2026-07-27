package com.camilo.scorefilm.auth.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 * EXPLICACIÓN GENERAL:
 * 
 * JwtAuthenticationFilter es un filtro que Spring ejecuta en CADA petición HTTP.
 * 
 * Su responsabilidad:
 * 1. Extrae el JWT del header Authorization
 * 2. Valida que el token sea válido
 * 3. Carga el usuario desde BD
 * 4. Setea la autenticación en SecurityContextHolder
 * 5. Permite que el request continúe
 * 
 * ¿Por qué OncePerRequestFilter?
 * - Garantiza que el filtro se ejecute UNA SOLA VEZ por request
 * - Previene ejecuciones múltiples
 * - Es la mejor práctica para filtros de autenticación
 * 
 * FLUJO DE UN REQUEST:
 * 
 * Cliente envía:
 * GET /api/users/me
 * Authorization: Bearer eyJhbGciOiJ...
 * 
 *     ↓
 * 
 * JwtAuthenticationFilter.doFilterInternal() se ejecuta
 * 
 *     ↓
 * 
 * [1] Extrae token del header
 *     "eyJhbGciOiJ..."
 * 
 *     ↓
 * 
 * [2] Extrae username del token
 *     "juan_perez"
 * 
 *     ↓
 * 
 * [3] Carga UserDetails de BD
 *     CustomUserDetails { username: "juan_perez", roles: [ADMIN] }
 * 
 *     ↓
 * 
 * [4] Valida que el token sea válido para ese usuario
 *     JwtService.isTokenValid(token, userDetails) → true
 * 
 *     ↓
 * 
 * [5] Crea Authentication y la setea en SecurityContext
 *     SecurityContextHolder.getContext().setAuthentication(...)
 * 
 *     ↓
 * 
 * [6] El request continúa hacia el controlador
 *     GET /api/users/me ejecuta
 * 
 *     ↓
 * 
 * [7] El controlador accede a SecurityContextHolder
 *     para obtener el usuario actual autenticado
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/.well-known/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        /*
         * PASO 1: EXTRAER TOKEN DEL HEADER
         * 
         * El cliente envía:
         * Authorization: Bearer eyJhbGciOiJ...
         * 
         * Desglosamos:
         * - Bearer = prefijo obligatorio
         * - eyJhbGciOiJ... = el token JWT
         */
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            // Verificar que el header exista
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                /*
                 * "Bearer eyJhbGciOiJ...".substring(7)
                 * = "eyJhbGciOiJ..."
                 * 
                 * Eliminamos los primeros 7 caracteres ("Bearer ")
                 */
                token = authHeader.substring(7);
                
                /*
                 * PASO 2: EXTRAER USERNAME DEL TOKEN
                 * 
                 * JwtService.extractUsername(token)
                 * - Decodifica el JWT
                 * - Extrae el campo "sub" (subject = username)
                 * - Lo retorna
                 * 
                 * Si el token es inválido, lanza excepción
                 * (la atrapamos abajo en el catch)
                 */
                username = jwtService.extractUsername(token);
                
                log.debug("Token extraído para usuario: {}", username);
            }
        } catch (Exception e) {
            /*
             * El token no se pudo procesar:
             * - Formato inválido
             * - Expirado
             * - Manipulado
             * 
             * Registramos el error pero continuamos
             * (el endpoint debería estar protegido y rechazará la petición)
             */
            log.warn("No se pudo extraer el JWT: {}", e.getMessage());
        }

        /*
         * PASO 3: VALIDAR Y CARGAR USUARIO
         * 
         * Si tenemos username y NO hay autenticación en el contexto
         * (para evitar procesamiento duplicado)
         */
        if (username != null && 
            SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                /*
                 * Cargar UserDetails desde BD
                 * 
                 * CustomUserDetailsService.loadUserByUsername(username)
                 * - Busca en BD
                 * - Retorna CustomUserDetails con roles, estado, etc
                 * 
                 * Si el usuario no existe, lanza UsernameNotFoundException
                 */
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                /*
                 * PASO 4: VALIDAR EL TOKEN
                 * 
                 * JwtService.isTokenValid(token, userDetails)
                 * 
                 * Verifica:
                 * 1. La firma es válida (no fue manipulado)
                 * 2. El username del token coincide con el usuario
                 * 3. El token no ha expirado
                 * 
                 * Si todo es correcto, retorna true
                 */
                if (jwtService.isTokenValid(token, userDetails)) {
                    
                    /*
                     * PASO 5: CREAR AUTHENTICATION Y GUARDARLA EN CONTEXTO
                     * 
                     * UsernamePasswordAuthenticationToken es un objeto que
                     * representa la autenticación del usuario.
                     * 
                     * Constructor:
                     * new UsernamePasswordAuthenticationToken(
                     *     principal,      // el usuario (UserDetails)
                     *     credentials,    // la contraseña (normalmente null en JWT)
                     *     authorities     // los roles (ROLE_ADMIN, ROLE_USER, etc)
                     * )
                     */
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,                          // principal
                            null,                                 // credentials (null porque usamos JWT)
                            userDetails.getAuthorities()          // authorities (roles)
                        );

                    /*
                     * WebAuthenticationDetailsSource crea detalles adicionales:
                     * - IP del cliente
                     * - User-Agent del navegador
                     * - Otros datos HTTP
                     */
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    /*
                     * PASO 6: GUARDAR EN SECURITY CONTEXT
                     * 
                     * SecurityContextHolder.getContext().setAuthentication(authentication)
                     * 
                     * Esto es CRUCIAL:
                     * - Ahora Spring Security SABE que este usuario está autenticado
                     * - Los @PreAuthorize("isAuthenticated()") funcionarán
                     * - El controlador puede acceder a este usuario via SecurityContextHolder
                     * 
                     * ¿Dónde se guarda?
                     * - En ThreadLocal (almacenamiento ligado al hilo actual)
                     * - Cada request es un hilo diferente
                     * - Por eso se limpia después del request (ver abajo)
                     */
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Usuario autenticado: {} con roles: {}", 
                        username, userDetails.getAuthorities());
                    
                } else {
                    log.warn("Token inválido para usuario: {}", username);
                }
                
            } catch (Exception e) {
                /*
                 * Errores posibles:
                 * - UsernameNotFoundException: usuario no existe en BD
                 * - Exception: otros errores (BD, JWT, etc)
                 * 
                 * Registramos pero continuamos
                 * El endpoint rechazará si requiere autenticación
                 */
                log.warn("Error al autenticar usuario: {}", e.getMessage());
            }
        }

        /*
         * PASO 7: CONTINUAR CON EL SIGUIENTE FILTRO
         * 
         * filterChain.doFilter() pasa el request al siguiente filtro
         * (o al controlador si este es el último filtro)
         */
        filterChain.doFilter(request, response);
    }
}
