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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class AuthFacade {
    public long JWT_VALIDITY_IN_MS = 1000 * 60 * 60 * 24; // 24 hours
    private String secretKey;
    private Set<String> invalidatedTokens;
    private String generateSecretKey() {
        try {
            // Generate a random input for the hash function
            SecureRandom random = new SecureRandom();
            byte[] randomBytes = new byte[32]; // 256 bits
            random.nextBytes(randomBytes);

            // Compute the SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(randomBytes);

            // Convert the hash bytes to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating secret key: SHA-256 algorithm not found", e);
        }
    }
    public  AuthFacade() {
        invalidatedTokens = new HashSet<String>();
        this.secretKey = "3cfa76ef14787c1c0ea519f8fc057b70fcd04a7420f8e8bcd0a7567c272e007b";
    }
    public AuthFacade(String key) {
        invalidatedTokens = new HashSet<String>();
        this.secretKey = key;
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