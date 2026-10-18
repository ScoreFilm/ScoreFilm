package com.camilo.scorefilm.auth.auth.controller;

import com.camilo.scorefilm.auth.config.JwtProperties;
import com.camilo.scorefilm.auth.config.RsaKeyProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwksControllerTest {
    @Test
    void getJwks_returnsPublicRsaMetadata() {
        RsaKeyProvider keys = mock(RsaKeyProvider.class);
        JwtProperties properties = new JwtProperties();
        properties.setKeyId("key-1");
        when(keys.getModulusBase64Url()).thenReturn("modulus");
        when(keys.getExponentBase64Url()).thenReturn("exponent");

        var result = new JwksController(keys, properties).getJwks();

        assertThat(result).containsKey("keys");
        assertThat(result.get("keys")).singleElement().satisfies(key -> {
            assertThat(key).containsEntry("kid", "key-1");
            assertThat(key).containsEntry("kty", "RSA");
            assertThat(key).containsEntry("alg", "RS256");
            assertThat(key).containsEntry("n", "modulus");
            assertThat(key).containsEntry("e", "exponent");
        });
    }
}
