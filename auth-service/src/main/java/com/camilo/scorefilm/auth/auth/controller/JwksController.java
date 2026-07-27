package com.camilo.scorefilm.auth.auth.controller;

import com.camilo.scorefilm.auth.config.JwtProperties;
import com.camilo.scorefilm.auth.config.RsaKeyProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class JwksController {
    private final RsaKeyProvider rsaKeyProvider;
    private final JwtProperties jwtProperties;

    public JwksController(RsaKeyProvider rsaKeyProvider, JwtProperties jwtProperties) {
        this.rsaKeyProvider = rsaKeyProvider;
        this.jwtProperties = jwtProperties;
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, String>>> getJwks() {
        return Map.of("keys", List.of(Map.of(
                "kty", "RSA", "alg", "RS256", "use", "sig", "kid", jwtProperties.getKeyId(),
                "n", rsaKeyProvider.getModulusBase64Url(), "e", rsaKeyProvider.getExponentBase64Url()
        )));
    }
}
