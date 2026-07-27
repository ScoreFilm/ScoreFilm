package com.camilo.scorefilm.auth.auth.security;

import com.camilo.scorefilm.auth.config.JwtProperties;
import com.camilo.scorefilm.auth.config.RsaKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;
    private final RsaKeyProvider rsaKeyProvider;

    public JwtService(JwtProperties jwtProperties, RsaKeyProvider rsaKeyProvider) {
        this.jwtProperties = jwtProperties;
        this.rsaKeyProvider = rsaKeyProvider;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        claims.put("roles", userDetails.getAuthorities());
        
        return generateToken(claims, userDetails.getUsername());
    }

    private String generateToken(Map<String, Object> claims, String username) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        
        log.debug("Generando JWT para usuario: {}, expira en: {}", 
            username, expiryDate);
        
        return Jwts.builder()
            .header().keyId(jwtProperties.getKeyId()).and()
            .claims(claims)
            .subject(username)                 
            .issuedAt(now)
            .expiration(expiryDate)
            
            .signWith(rsaKeyProvider.getPrivateKey(), Jwts.SIG.RS256)
            
            .compact();
    }

    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            
            boolean usernameMatches = username.equals(userDetails.getUsername());
            
            boolean notExpired = !isTokenExpired(token);
            
            return usernameMatches && notExpired;
            
        } catch (JwtException e) {
            log.warn("JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(rsaKeyProvider.getPublicKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}
