package com.camilo.scorefilm.auth.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RsaKeyProviderTest {
    @TempDir Path tempDir;

    @Test
    void initialize_missingFiles_generatesAndPersistsKeyPair() {
        JwtProperties properties = propertiesAt(tempDir.resolve("private.pem"), tempDir.resolve("public.pem"));
        RsaKeyProvider provider = new RsaKeyProvider(properties);

        provider.initialize();

        assertThat(provider.getPrivateKey()).isNotNull();
        assertThat(provider.getPublicKey()).isNotNull();
        assertThat(provider.getModulusBase64Url()).isNotBlank();
        assertThat(provider.getExponentBase64Url()).isNotBlank();
        assertThat(Files.exists(Path.of(properties.getPrivateKeyPath()))).isTrue();
        assertThat(Files.exists(Path.of(properties.getPublicKeyPath()))).isTrue();
    }

    @Test
    void initialize_existingFiles_loadsSameKeyPair() {
        JwtProperties properties = propertiesAt(tempDir.resolve("private.pem"), tempDir.resolve("public.pem"));
        RsaKeyProvider generated = new RsaKeyProvider(properties);
        generated.initialize();
        String modulus = generated.getModulusBase64Url();

        RsaKeyProvider loaded = new RsaKeyProvider(properties);
        loaded.initialize();

        assertThat(loaded.getModulusBase64Url()).isEqualTo(modulus);
        assertThat(loaded.getExponentBase64Url()).isEqualTo(generated.getExponentBase64Url());
    }

    @Test
    void initialize_invalidPem_failsWithContext() throws Exception {
        Path privatePath = tempDir.resolve("private.pem");
        Path publicPath = tempDir.resolve("public.pem");
        Files.writeString(privatePath, "invalid");
        Files.writeString(publicPath, "invalid");

        assertThatThrownBy(() -> new RsaKeyProvider(propertiesAt(privatePath, publicPath)).initialize())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to initialize RSA keys");
    }

    private JwtProperties propertiesAt(Path privatePath, Path publicPath) {
        JwtProperties properties = new JwtProperties();
        properties.setPrivateKeyPath(privatePath.toString());
        properties.setPublicKeyPath(publicPath.toString());
        return properties;
    }
}
