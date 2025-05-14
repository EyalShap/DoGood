package com.dogood.dogoodbackend.domain.users;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Cryptography {
    public static String hashString(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(hash);
            return encoded;
        } catch (NoSuchAlgorithmException ignore) {}
        return string;
    }
    /**
     * Hashes a password using the hashString method.
     * WARNING: Uses an insecure hashing method (hashString).
     * For secure password storage, use BCrypt, SCrypt, or Argon2.
     * @param rawPassword The plain text password.
     * @return The hashed password.
     */
    public static String hashPassword(String rawPassword) {
        // Directly use the provided hashString method
        return hashString(rawPassword);
    }

    /**
     * Checks a raw password against a stored hashed password.
     * WARNING: Uses an insecure hashing method (hashString).
     * @param rawPassword The plain text password to check.
     * @param hashedPassword The stored hashed password.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        // Hash the raw password using the same method and compare
        String hashedRawPassword = hashString(rawPassword);
        return hashedPassword.equals(hashedRawPassword);
    }
}
