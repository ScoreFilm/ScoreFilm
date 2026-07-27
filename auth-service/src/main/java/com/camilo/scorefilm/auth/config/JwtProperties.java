package com.camilo.scorefilm.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * EXPLICACIÓN:
 * 
 * @ConfigurationProperties mapea propiedades de application.yaml
 * a atributos de una clase.
 * 
 * prefix = "app.jwt" significa que busca en YAML:
 * app:
 *   jwt:
 *     private-key-path: ...
 *     public-key-path: ...
 *     expiration: ...
 *     refresh-expiration: ...
 * 
 * Los nombres de las propiedades YAML se convierten automáticamente
 * a camelCase:
 * - secret-key (YAML) → secretKey (Java)
 * - refresh-expiration (YAML) → refreshExpiration (Java)
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
    private String privateKeyPath = "src/main/resources/certs/jwt-private.pem";
    private String publicKeyPath = "src/main/resources/certs/jwt-public.pem";
    private String keyId = "scorefilm-key-id";
    private long expiration;
    private long refreshExpiration;
}
