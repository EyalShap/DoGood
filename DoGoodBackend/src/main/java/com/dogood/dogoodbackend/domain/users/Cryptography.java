package com.dogood.dogoodbackend.domain.users;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Cryptography {
    final static int WORK_FACTOR = 10;

    public static String hashStringOld(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(hash);
            return encoded;
        } catch (NoSuchAlgorithmException ignore) {}
        return string;
    }

    public static String hashString(String string) {
        //bcrypt hash
        return BCrypt.hashpw(string,BCrypt.gensalt(WORK_FACTOR));
    }

    public static String hashPassword(String rawPassword) {
        // Directly use the provided hashString method
        return hashString(rawPassword);
    }

    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        if (hashedPassword.contains("$")) { // bcrypt check
            return BCrypt.checkpw(rawPassword, hashedPassword);
        }
        // Hash the raw password using the same method and compare
        String hashedRawPassword = hashString(rawPassword);
        return hashedPassword.equals(hashedRawPassword);
    }
}
