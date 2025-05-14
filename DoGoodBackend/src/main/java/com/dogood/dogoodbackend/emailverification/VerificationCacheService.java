package com.dogood.dogoodbackend.emailverification;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest; // Ensure this import is correct

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.Duration; // Ensure this import is present
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// The VerificationData record is now in its own file (VerificationData.java)

@Service
public class VerificationCacheService {

    // Now correctly refers to the top-level VerificationData record
    private final ConcurrentHashMap<String, VerificationData> verificationCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long CLEANUP_INTERVAL_MINUTES = 5;
    private static final long ENTRY_EXPIRY_MINUTES = 5;

    public VerificationCacheService() {
        cleanupScheduler.scheduleAtFixedRate(this::removeExpiredEntries,
                CLEANUP_INTERVAL_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
        System.out.println("Verification Cache Service Initialized. Cleanup scheduled.");
    }

    /**
     * Stores the user's registration data along with a verification code and expiry.
     * The password in userData within RegisterRequest should already be hashed.
     * @param emailKey The email address (lowercased) to use as the cache key.
     * @param userData The original registration request data (with hashed password).
     * @param code The generated verification code.
     */
    public void storeVerificationData(String emailKey, RegisterRequest userData, String code) {
        Instant expiry = Instant.now().plus(Duration.ofMinutes(ENTRY_EXPIRY_MINUTES));
        // The RegisterRequest DTO passed to VerificationData should already have the password hashed
        VerificationData dataToCache = new VerificationData(code, expiry, userData);
        verificationCache.put(emailKey.toLowerCase(), dataToCache);
        System.out.println("Stored verification data for: " + emailKey.toLowerCase() + " with code " + code);
    }

    /**
     * Retrieves verification data if the emailKey exists, the entry hasn't expired,
     * AND the submittedCode matches the stored code.
     * Removes expired or mismatched code entries from the cache.
     * @param emailKey The email of the user.
     * @param submittedCode The code submitted by the user.
     * @return Optional containing VerificationData if valid, empty otherwise.
     */
    public Optional<VerificationData> getAndValidateVerificationData(String emailKey, String submittedCode) {
        String lowerCaseEmail = emailKey.toLowerCase();
        VerificationData data = verificationCache.get(lowerCaseEmail);

        if (data == null) {
            System.out.println("No verification data found for: " + lowerCaseEmail);
            return Optional.empty();
        }

        System.out.println("Found verification data for: " + lowerCaseEmail + ". Validating...");

        if (data.expiry().isBefore(Instant.now())) {
            System.out.println("Verification data EXPIRED for: " + lowerCaseEmail + ". Removing from cache.");
            verificationCache.remove(lowerCaseEmail); // Clean up expired entry
            return Optional.empty();
        }

        if (!data.code().equals(submittedCode)) {
            System.out.println("Invalid code submitted for: " + lowerCaseEmail + ". Expected: " + data.code() + ", Got: " + submittedCode);
            // Optional: Implement attempt tracking here. For now, just invalidate by returning empty.
            // You might *not* want to remove it on a single wrong attempt to allow retries,
            // but then you'd need separate logic for a "too many attempts" scenario.
            // For simplicity here, let's say an invalid code means the entry is consumed or suspect.
            // Or, to allow multiple tries for the *same* code until expiry:
            // verificationCache.remove(lowerCaseEmail); // Comment this out if multiple attempts are allowed
            return Optional.empty();
        }

        System.out.println("Code verified successfully for: " + lowerCaseEmail);
        // IMPORTANT: Do NOT remove from cache here. Facade will remove AFTER successful user creation.
        return Optional.of(data);
    }

    /**
     * Retrieves verification data primarily for checking existence or expiry,
     * without validating a code.
     * Removes the entry if it's expired.
     * @param emailKey The email of the user.
     * @return Optional containing VerificationData if exists and not expired, empty otherwise.
     */
    public Optional<VerificationData> getVerificationData(String emailKey) {
        String lowerCaseEmail = emailKey.toLowerCase();
        VerificationData data = verificationCache.get(lowerCaseEmail);
        if (data != null) {
            if (data.expiry().isBefore(Instant.now())) {
                verificationCache.remove(lowerCaseEmail);
                return Optional.empty();
            }
            return Optional.of(data);
        }
        return Optional.empty();
    }


    public void removeVerificationData(String emailKey) {
        verificationCache.remove(emailKey.toLowerCase());
        System.out.println("Explicitly removed verification data for: " + emailKey.toLowerCase());
    }

    private void removeExpiredEntries() {
        Instant now = Instant.now();
        int initialSize = verificationCache.size();
        verificationCache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().expiry().isBefore(now);
            if (expired) {
                System.out.println("Cache cleanup removing expired entry for: " + entry.getKey());
            }
            return expired;
        });
        int finalSize = verificationCache.size();
        if (initialSize > 0 || finalSize < initialSize) {
            System.out.printf("Ran verification cache cleanup. Removed %d expired entries. Current cache size: %d%n", initialSize - finalSize, finalSize);
        }
    }

    @PreDestroy
    public void shutdown() {
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Verification Cache Service Shutting Down. Cleanup scheduler stopped.");
    }
}