package com.camilo.scorefilm.auth.auth.controller;

import com.camilo.scorefilm.auth.auth.dto.AuthResponse;
import com.camilo.scorefilm.auth.auth.dto.LoginRequest;
import com.camilo.scorefilm.auth.auth.dto.RegisterRequest;
import com.camilo.scorefilm.auth.auth.service.AuthService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    private final AuthService authService = mock(AuthService.class);
    private final AuthController controller = new AuthController(authService);

    @Test
    void register_validRequest_returnsCreatedResponseWithLinks() {
        RegisterRequest request = RegisterRequest.builder().username("camilo").email("c@example.com").build();
        AuthResponse auth = AuthResponse.builder().username("camilo").email("c@example.com").build();
        when(authService.register(request)).thenReturn(auth);

        var response = controller.register(request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().getMessage()).isEqualTo("Usuario registrado exitosamente");
        assertThat(response.getBody().getData().getLinks()).hasSize(2);
        assertThat(response.getBody().getData().getLink("login")).isPresent();
    }

    @Test
    void login_validRequest_returnsOkResponseWithLinks() {
        LoginRequest request = LoginRequest.builder().email("c@example.com").password("Password123").build();
        AuthResponse auth = AuthResponse.builder().username("camilo").email("c@example.com").build();
        when(authService.login(request)).thenReturn(auth);

        var response = controller.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Login exitoso");
        assertThat(response.getBody().getData().getLinks()).hasSize(2);
        assertThat(response.getBody().getData().getLink("self")).isPresent();
    }
}
