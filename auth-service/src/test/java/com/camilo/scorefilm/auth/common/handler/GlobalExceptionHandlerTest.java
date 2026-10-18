package com.camilo.scorefilm.auth.common.handler;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void authenticationExceptions_returnUnauthorized() {
        assertThat(handler.handleBadCredentials(new BadCredentialsException("bad"), null).getStatusCode().value()).isEqualTo(401);
        assertThat(handler.handleUsernameNotFound(new UsernameNotFoundException("missing"), null).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void accessDenied_returnsForbidden() {
        var response = handler.handleAccessDenied(new AccessDeniedException("denied"), null);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getMessage()).contains("permisos");
    }

    @Test
    void runtimeMessages_mapToConflictNotFoundOrInternalError() {
        assertThat(handler.handleRuntimeException(new RuntimeException("ya existe"), null).getStatusCode().value()).isEqualTo(409);
        assertThat(handler.handleRuntimeException(new RuntimeException("no encontrado"), null).getStatusCode().value()).isEqualTo(404);
        assertThat(handler.handleRuntimeException(new RuntimeException((String) null), null).getStatusCode().value()).isEqualTo(500);
        assertThat(handler.handleRuntimeException(new RuntimeException("unexpected"), null).getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void genericException_returnsInternalServerError() {
        var response = handler.handleGenericException(new Exception("unexpected"), null);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).contains("inesperado");
    }

    @Test
    void validationException_returnsBadRequest() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        var binding = mock(org.springframework.validation.BindingResult.class);
        whenBinding(exception, binding);

        var response = handler.handleValidationException(exception, null);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Validacion fallida");
    }

    private void whenBinding(MethodArgumentNotValidException exception,
                             org.springframework.validation.BindingResult binding) {
        org.mockito.Mockito.when(exception.getBindingResult()).thenReturn(binding);
        org.mockito.Mockito.when(binding.getFieldErrors()).thenReturn(java.util.List.of());
    }
}
