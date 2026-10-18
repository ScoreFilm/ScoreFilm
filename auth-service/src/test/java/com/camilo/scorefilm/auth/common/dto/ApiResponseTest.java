package com.camilo.scorefilm.auth.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void factoryMethods_expectedStatusAndMessage() {
        assertThat(ApiResponse.success("data", "ok")).satisfies(response -> {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getData()).isEqualTo("data");
            assertThat(response.getTimestamp()).isNotNull();
        });
        assertThat(ApiResponse.created("data", "created").getStatus()).isEqualTo(201);
        assertThat(ApiResponse.badRequest("bad").getStatus()).isEqualTo(400);
        assertThat(ApiResponse.unauthorized("unauthorized").getStatus()).isEqualTo(401);
        assertThat(ApiResponse.forbidden("forbidden").getStatus()).isEqualTo(403);
        assertThat(ApiResponse.notFound("missing").getStatus()).isEqualTo(404);
        assertThat(ApiResponse.conflict("conflict").getStatus()).isEqualTo(409);
        assertThat(ApiResponse.internalServerError("error").getStatus()).isEqualTo(500);
    }
}
