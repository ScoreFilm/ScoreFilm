package com.camilo.scorefilm.auth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /*
     * Código de estado HTTP
     * 200: OK
     * 201: Created
     * 400: Bad Request
     * 401: Unauthorized
     * 403: Forbidden
     * 404: Not Found
     * 409: Conflict
     * 500: Internal Server Error
     */
    @JsonProperty("status")
    private Integer status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /*
     * SUCCESS: 200 OK
     * Usado para operaciones exitosas (GET, update, etc.)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .status(200)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /*
     * CREATED: 201 Created
     * Usado para POST que crean recursos exitosamente
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
            .status(201)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /*
     * BAD REQUEST: 400 Bad Request
     * Usado cuando los datos del cliente son inválidos
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
            .status(400)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /*
     * UNAUTHORIZED: 401 Unauthorized
     * Usado cuando faltan credenciales o son inválidas
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
            .status(401)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /*
     * FORBIDDEN: 403 Forbidden
     * Usado cuando el usuario no tiene permisos
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
            .status(403)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /*
     * NOT FOUND: 404 Not Found
     * Usado cuando el recurso no existe
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
            .status(404)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /*
     * CONFLICT: 409 Conflict
     * Usado cuando hay conflictos (email ya existe, etc.)
     */
    public static <T> ApiResponse<T> conflict(String message) {
        return ApiResponse.<T>builder()
            .status(409)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /*
     * INTERNAL SERVER ERROR: 500 Internal Server Error
     * Usado cuando hay errores del servidor
     */
    public static <T> ApiResponse<T> internalServerError(String message) {
        return ApiResponse.<T>builder()
            .status(500)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
