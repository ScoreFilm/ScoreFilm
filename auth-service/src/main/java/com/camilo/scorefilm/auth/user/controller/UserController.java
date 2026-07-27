package com.camilo.scorefilm.auth.user.controller;

import com.camilo.scorefilm.auth.auth.security.CustomUserDetails;
import com.camilo.scorefilm.auth.common.dto.ApiResponse;
import com.camilo.scorefilm.auth.user.dto.UserResponse;
import com.camilo.scorefilm.auth.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * EXPLICACIÓN:
 * 
 * UserController expone endpoints de usuario.
 * 
 * @RestController = endpoint REST
 * @RequestMapping = base URL
 * @Slf4j = logger
 * 
 * Los métodos con @PreAuthorize requieren autenticación
 * y Spring valida automáticamente antes de ejecutar.
 * 
 * Nota: CORS está configurado globalmente en CorsConfig
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ========== ENDPOINT: GET /api/users/me ==========

    /*
     * GET /api/users/me
     * 
     * Devuelve los datos del usuario actualmente autenticado.
     * 
     * @PreAuthorize("isAuthenticated()") = SOLO usuarios autenticados
     * 
     * ¿Cómo funciona?
     * 
     * 1. Cliente envía:
     *    GET /api/users/me
     *    Authorization: Bearer eyJhbGciOiJ...
     * 
     * 2. JwtAuthenticationFilter valida el JWT y setea SecurityContext
     * 
     * 3. Spring ve @PreAuthorize("isAuthenticated()")
     *    → Verifica si hay Authentication en SecurityContext
     *    → Si NO hay, rechaza con 403 Forbidden
     *    → Si hay, continúa
     * 
     * 4. El controlador accede a Authentication
     *    → Obtiene el usuario del principal
     *    → Devuelve sus datos
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        log.info("GET /users/me - Usuario: {}", authentication.getName());

        /*
         * EXPLICACIÓN DEL PARÁMETRO Authentication:
         * 
         * Spring inyecta automáticamente el Authentication del SecurityContext
         * gracias a su integración con la seguridad.
         * 
         * Authentication contiene:
         * - principal: el objeto UserDetails (CustomUserDetails)
         * - authorities: los roles del usuario
         * - authenticated: true (porque pasa @PreAuthorize)
         * 
         * ¿Por qué CustomUserDetails?
         * 
         * En JwtAuthenticationFilter hicimos:
         * new UsernamePasswordAuthenticationToken(
         *     userDetails,    // ← CustomUserDetails como principal
         *     null,
         *     userDetails.getAuthorities()
         * )
         */
        
        // Obtener UserDetails del Authentication
        Object principal = authentication.getPrincipal();
        
        if (!(principal instanceof CustomUserDetails)) {
            log.error("Principal no es CustomUserDetails");
            return ResponseEntity.internalServerError()
                .body(ApiResponse.internalServerError("Error interno del servidor"));
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) principal;
        
        /*
         * Obtener el username y cargar el usuario completo de BD
         * 
         * ¿Por qué?
         * - CustomUserDetails solo tiene datos de seguridad
         * - UserService tiene acceso a toda la entidad User
         * - Queremos devolver firstName, lastName, roles con nombres, etc
         */
        String username = customUserDetails.getUsername();
        UserResponse userResponse = userService.getUserByUsername(username);

        // Agregar links HATEOAS
        userResponse.add(
            WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getCurrentUser(authentication))
                .withSelfRel()
        );

        ApiResponse<UserResponse> apiResponse = ApiResponse.success(
            userResponse,
            "Datos del usuario obtenidos exitosamente"
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminOnly(Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.success(
                authentication.getName(),
                "Endpoint disponible solo para ADMIN"
            )
        );
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> userOrAdmin(Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.success(
                authentication.getName(),
                "Endpoint disponible para USER y ADMIN"
            )
        );
    }

    /*
     * NOTA IMPORTANTE: Importa este cambio
     * Necesitamos importar CustomUserDetails aquí
     */
}
