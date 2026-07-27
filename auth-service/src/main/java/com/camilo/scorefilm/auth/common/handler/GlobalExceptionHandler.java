package com.camilo.scorefilm.auth.common.handler;

import com.camilo.scorefilm.auth.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request
    ) {
        log.warn("Intento de login con credenciales invalidas");

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.unauthorized("Email o contraseña incorrectos"));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFound(
            UsernameNotFoundException ex,
            WebRequest request
    ) {
        log.warn("Usuario no encontrado durante autenticacion");

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.unauthorized("Credenciales invalidas"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request
    ) {
        log.warn("Acceso denegado: {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.forbidden("No tienes permisos para acceder a este recurso"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        log.warn("Error de validacion en request");

        Map<String, List<String>> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.groupingBy(
                error -> error.getField(),
                Collectors.mapping(error -> error.getDefaultMessage(), Collectors.toList())
            ));

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.<Map<String, List<String>>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validacion fallida")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex,
            WebRequest request
    ) {
        log.error("Error en logica de negocio: {}", ex.getMessage(), ex);

        String message = ex.getMessage();

        if (message != null) {
            if (message.contains("ya existe") || message.contains("ya está registrado")) {
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.conflict(message));
            }

            if (message.contains("no encontrado") || message.contains("no existe")) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(message));
            }
        }

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.internalServerError(
                "Ocurrio un error interno. Por favor, intenta mas tarde."
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Excepcion inesperada: {}", ex.getMessage(), ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.internalServerError(
                "Ocurrio un error inesperado. Por favor, intenta mas tarde."
            ));
    }
}
