package com.camilo.scorefilm.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void corsConfigurationSource_configuredOrigins_appliesRestrictiveApiPolicy() {
        List<String> origins = List.of("https://app.example.com", "http://localhost:5173");

        CorsConfigurationSource source = new CorsConfig().corsConfigurationSource(origins);
        CorsConfiguration configuration = source.getCorsConfiguration(request("/api/auth/login"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactlyElementsOf(origins);
        assertThat(configuration.getAllowedMethods())
                .containsExactly("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertThat(configuration.getAllowedHeaders())
                .containsExactly("Authorization", "Content-Type", "Accept");
        assertThat(configuration.getAllowCredentials()).isFalse();
        assertThat(configuration.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void corsConfigurationSource_originChecks_allowConfiguredOriginAndRejectUnknownOrigin() {
        CorsConfigurationSource source = new CorsConfig().corsConfigurationSource(
                List.of("https://app.example.com"));
        CorsConfiguration configuration = source.getCorsConfiguration(request("/api/users/me"));

        assertThat(configuration.checkOrigin("https://app.example.com"))
                .isEqualTo("https://app.example.com");
        assertThat(configuration.checkOrigin("https://evil.example.com")).isNull();
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }
}
