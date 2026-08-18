package com.camilo.scorefilm.auth.auth.security;

import com.camilo.scorefilm.auth.config.JwtProperties;
import com.camilo.scorefilm.auth.config.RsaKeyProvider;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String USERNAME = "camilo";

    private JwtProperties jwtProperties;
    private RsaKeyProvider rsaKeyProvider;
    private JwtService jwtService;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        keyPair = generateKeyPair();
        jwtProperties = new JwtProperties();
        jwtProperties.setKeyId("test-key-id");
        jwtProperties.setExpiration(60_000L);
        rsaKeyProvider = rsaKeyProviderFor(keyPair);
        jwtService = new JwtService(jwtProperties, rsaKeyProvider);
    }

    @Test
    @DisplayName("should generate a parsable signed JWT for valid user details")
    void generateToken_validUserDetails_returnsParsableJwt() {
        // Arrange
        UserDetails userDetails = userDetails(USERNAME);

        // Act
        String token = jwtService.generateToken(userDetails);
        var parsed = Jwts.parser()
                .verifyWith((RSAPublicKey) keyPair.getPublic())
                .build()
                .parseSignedClaims(token);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(parsed.getPayload().getSubject()).isEqualTo(USERNAME);
        assertThat(parsed.getHeader().getKeyId()).isEqualTo("test-key-id");
        assertThat(parsed.getPayload().get("roles")).isNotNull();
    }

    @Test
    @DisplayName("should validate a token when the subject and signature are correct")
    void isTokenValid_matchingTokenAndUser_returnsTrue() {
        // Arrange
        UserDetails userDetails = userDetails(USERNAME);
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean valid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("should reject an expired token")
    void isTokenValid_expiredToken_returnsFalse() throws Exception {
        // Arrange
        UserDetails userDetails = userDetails(USERNAME);
        JwtService expiredJwtService = new JwtService(expiredProperties(), rsaKeyProviderFor(keyPair));
        String expiredToken = expiredJwtService.generateToken(userDetails);

        // Act
        boolean valid = jwtService.isTokenValid(expiredToken, userDetails);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("should reject a token signed with another RSA key")
    void isTokenValid_tamperedToken_returnsFalse() throws Exception {
        // Arrange
        UserDetails userDetails = userDetails(USERNAME);
        KeyPair otherKeyPair = generateKeyPair();
        JwtService foreignJwtService = new JwtService(jwtProperties, rsaKeyProviderFor(otherKeyPair));
        String tamperedToken = foreignJwtService.generateToken(userDetails);

        // Act
        boolean valid = jwtService.isTokenValid(tamperedToken, userDetails);

        // Assert
        assertThat(valid).isFalse();
    }

    private JwtProperties expiredProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setKeyId("test-key-id");
        properties.setExpiration(-1L);
        return properties;
    }

    private UserDetails userDetails(String username) {
        return User.withUsername(username)
                .password("ignored")
                .authorities("ROLE_USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private RsaKeyProvider rsaKeyProviderFor(KeyPair pair) {
        RsaKeyProvider provider = mock(RsaKeyProvider.class);
        when(provider.getPrivateKey()).thenReturn(pair.getPrivate());
        when(provider.getPublicKey()).thenReturn((RSAPublicKey) pair.getPublic());
        return provider;
    }
}
