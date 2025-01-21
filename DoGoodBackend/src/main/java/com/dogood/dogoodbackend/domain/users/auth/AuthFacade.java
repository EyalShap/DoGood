package com.dogood.dogoodbackend.domain.users.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AuthFacade {
    public int JWT_VALIDITY_IN_MS = 1000 * 60;
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    private Set<String> invalidatedTokens;

    public AuthFacade() {
        invalidatedTokens = new HashSet<String>();
    }

    public String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_VALIDITY_IN_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        return token;
    }

    private Key getSigningKey() {
        byte[] keyBytes = this.secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean verifyToken(String token) {
        if (invalidatedTokens.contains(token)) {
            return false;
        }
        // Verifying a JWT
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJwt(token)
                .getBody();
        return !claims.isEmpty();
    }

    public String getNameFromToken(String token) {
        if (invalidatedTokens.contains(token)) {
            throw new IllegalArgumentException("Given token is invalid.");
        }
        // Verifying a JWT
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public void invalidateToken(String token) {
        if (!invalidatedTokens.contains(token)) {
            invalidatedTokens.add(token);
        } else {
            throw new IllegalArgumentException("Given token is already logged out of the system.");
        }
    }
}