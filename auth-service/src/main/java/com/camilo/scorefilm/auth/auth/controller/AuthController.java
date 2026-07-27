package com.camilo.scorefilm.auth.auth.controller;

import com.camilo.scorefilm.auth.auth.dto.AuthResponse;
import com.camilo.scorefilm.auth.auth.dto.LoginRequest;
import com.camilo.scorefilm.auth.auth.dto.RegisterRequest;
import com.camilo.scorefilm.auth.auth.service.AuthService;
import com.camilo.scorefilm.auth.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("POST /auth/register - username: {}", request.getUsername());
        
        AuthResponse response = authService.register(request);
        
        response.add(
            WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).login(new LoginRequest()))
                .withRel("login"),
            WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).register(request))
                .withSelfRel()
        );
        
        ApiResponse<AuthResponse> apiResponse = ApiResponse.created(
            response,
            "Usuario registrado exitosamente"
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("POST /auth/login - email: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        response.add(
            WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).login(request))
                .withSelfRel(),
            WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).register(new RegisterRequest()))
                .withRel("register")
        );
        
        ApiResponse<AuthResponse> apiResponse = ApiResponse.success(
            response,
            "Login exitoso"
        );
        
        return ResponseEntity.ok(apiResponse);
    }
}
