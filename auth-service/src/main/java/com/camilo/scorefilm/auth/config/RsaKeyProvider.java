package com.camilo.scorefilm.auth.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

/** Loads the RSA key pair used to sign access tokens, creating it on first startup. */
@Component
@Slf4j
public class RsaKeyProvider {
    private static final Base64.Encoder PEM_ENCODER = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII));
    private final JwtProperties properties;
    private PrivateKey privateKey;
    private RSAPublicKey publicKey;

    public RsaKeyProvider(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initialize() {
        Path privateKeyPath = Path.of(properties.getPrivateKeyPath()).toAbsolutePath().normalize();
        Path publicKeyPath = Path.of(properties.getPublicKeyPath()).toAbsolutePath().normalize();
        try {
            if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
                privateKey = readPrivateKey(privateKeyPath);
                publicKey = readPublicKey(publicKeyPath);
                log.info("JWT RSA key pair loaded from {} and {}", privateKeyPath, publicKeyPath);
                return;
            }
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            writePem(privateKeyPath, "PRIVATE KEY", privateKey.getEncoded());
            writePem(publicKeyPath, "PUBLIC KEY", publicKey.getEncoded());
            log.info("Generated a new 2048-bit RSA JWT key pair at {} and {}", privateKeyPath, publicKeyPath);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize RSA keys for JWT signing", exception);
        }
    }

    public PrivateKey getPrivateKey() { return privateKey; }
    public RSAPublicKey getPublicKey() { return publicKey; }
    public String getModulusBase64Url() { return base64UrlUnsigned(publicKey.getModulus()); }
    public String getExponentBase64Url() { return base64UrlUnsigned(publicKey.getPublicExponent()); }

    private PrivateKey readPrivateKey(Path path) throws Exception {
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(readPem(path, "PRIVATE KEY")));
    }

    private RSAPublicKey readPublicKey(Path path) throws Exception {
        PublicKey key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(readPem(path, "PUBLIC KEY")));
        return (RSAPublicKey) key;
    }

    private byte[] readPem(Path path, String type) throws IOException {
        String content = Files.readString(path, StandardCharsets.US_ASCII)
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(content);
    }

    private void writePem(Path path, String type, byte[] encoded) throws IOException {
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        String pem = "-----BEGIN " + type + "-----\n" + PEM_ENCODER.encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
        Files.writeString(path, pem, StandardCharsets.US_ASCII);
    }

    private String base64UrlUnsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        int offset = bytes.length > 1 && bytes[0] == 0 ? 1 : 0;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOfRange(bytes, offset, bytes.length));
    }
}
