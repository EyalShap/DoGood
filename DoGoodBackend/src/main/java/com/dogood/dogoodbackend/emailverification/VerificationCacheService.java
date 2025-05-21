package com.dogood.dogoodbackend.emailverification;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest; // Ensure this import is correct

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCacheService {

    // Cache for initial registration verification and resend (for registration/forgot password)
    // Keyed by USERNAME.
    private final ConcurrentHashMap<String, VerificationData> verificationCache = new ConcurrentHashMap<>();

    // Cache specifically for verifying a user's current email before an update (e.g., email change)
    // Keyed by USERNAME of the authenticated user performing the action.
    private final ConcurrentHashMap<String, UpdateVerificationCodeEntry> emailUpdateVerificationCache = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long CLEANUP_INTERVAL_MINUTES = 5;
    private static final long ENTRY_EXPIRY_MINUTES = 5; // Default expiry for codes

    // Record for storing codes for email update verification
    record UpdateVerificationCodeEntry(String code, Instant expiry) {}

    public VerificationCacheService() {
        cleanupScheduler.scheduleAtFixedRate(this::removeExpiredEntries,
                CLEANUP_INTERVAL_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
        System.out.println("Verification Cache Service Initialized. Cleanup scheduled.");
    }

    /**
     * Stores verification data (typically for initial registration or forgot password flows).
     * The password in userData within RegisterRequest should already be hashed by the caller.
     * @param usernameKey The username (lowercased) to use as the cache key.
     * @param userData The user data (e.g., from RegisterRequest or constructed for context).
     * @param code The generated verification code.
     */
    public void storeVerificationData(String usernameKey, RegisterRequest userData, String code) {
        Instant expiry = Instant.now().plus(Duration.ofMinutes(ENTRY_EXPIRY_MINUTES));
        VerificationData dataToCache = new VerificationData(code, expiry, userData);
        verificationCache.put(usernameKey.toLowerCase(), dataToCache);
        System.out.println("Stored verification data for username: " + usernameKey.toLowerCase() + " with code " + code);
    }

    /**
     * Retrieves and validates verification data from the main cache.
     * Used for initial registration verification or forgot password code validation.
     * @param usernameKey The username (lowercased) of the user.
     * @param submittedCode The code submitted by the user.
     * @return Optional containing VerificationData if valid, empty otherwise.
     */
    public Optional<VerificationData> getAndValidateVerificationData(String usernameKey, String submittedCode) {
        String lowerCaseUsername = usernameKey.toLowerCase();
        VerificationData data = verificationCache.get(lowerCaseUsername);

        if (data == null) {
            System.out.println("No verification data found for username: " + lowerCaseUsername);
            return Optional.empty();
        }

        System.out.println("Found verification data for username: " + lowerCaseUsername + ". Validating...");

        if (data.expiry().isBefore(Instant.now())) {
            System.out.println("Verification data EXPIRED for username: " + lowerCaseUsername + ". Removing from cache.");
            verificationCache.remove(lowerCaseUsername);
            return Optional.empty();
        }

        if (!data.code().equals(submittedCode)) {
            System.out.println("Invalid code submitted for username: " + lowerCaseUsername + ". Expected: " + data.code() + ", Got: " + submittedCode);
            return Optional.empty();
        }

        System.out.println("Code verified successfully for username: " + lowerCaseUsername);
        return Optional.of(data);
    }

    /**
     * Retrieves verification data from the main cache without validating a code.
     * Useful for checking existence or if a resend is permissible.
     * Removes the entry if it's expired.
     * @param usernameKey The username (lowercased) of the user.
     * @return Optional containing VerificationData if exists and not expired, empty otherwise.
     */
    public Optional<VerificationData> getVerificationData(String usernameKey) {
        String lowerCaseUsername = usernameKey.toLowerCase();
        VerificationData data = verificationCache.get(lowerCaseUsername);
        if (data != null) {
            if (data.expiry().isBefore(Instant.now())) {
                verificationCache.remove(lowerCaseUsername);
                return Optional.empty();
            }
            return Optional.of(data);
        }
        return Optional.empty();
    }

    /**
     * Explicitly removes verification data from the main cache.
     * @param usernameKey The username (lowercased) whose data should be removed.
     */
    public void removeVerificationData(String usernameKey) {
        verificationCache.remove(usernameKey.toLowerCase());
        System.out.println("Explicitly removed verification data for username: " + usernameKey.toLowerCase());
    }

    // --- Methods for Email Update Process Verification ---

    /**
     * Stores a verification code for an email update operation, keyed by the actor's username.
     * This is for when a logged-in user wants to change their email and needs to verify their current email.
     * @param actorUsernameKey The username (lowercased) of the authenticated user requesting the update.
     * @param code The generated verification code.
     */
    public void storeEmailUpdateVerificationCode(String actorUsernameKey, String code) {
        Instant expiry = Instant.now().plus(Duration.ofMinutes(ENTRY_EXPIRY_MINUTES));
        UpdateVerificationCodeEntry entry = new UpdateVerificationCodeEntry(code, expiry);
        emailUpdateVerificationCache.put(actorUsernameKey.toLowerCase(), entry);
        System.out.println("Stored email update verification code for user: " + actorUsernameKey.toLowerCase());
    }

    /**
     * Retrieves and validates an email update verification code, keyed by the actor's username.
     * @param actorUsernameKey The username (lowercased) of the authenticated user.
     * @param submittedCode The code submitted by the user.
     * @return true if the code is valid and not expired, false otherwise.
     */
    public boolean getAndValidateEmailUpdateVerificationCode(String actorUsernameKey, String submittedCode) {
        String lowerCaseUsername = actorUsernameKey.toLowerCase();
        UpdateVerificationCodeEntry entry = emailUpdateVerificationCache.get(lowerCaseUsername);

        if (entry == null) {
            System.out.println("No email update verification code found for user: " + lowerCaseUsername);
            return false;
        }

        if (entry.expiry().isBefore(Instant.now())) {
            System.out.println("Email update verification code EXPIRED for user: " + lowerCaseUsername);
            emailUpdateVerificationCache.remove(lowerCaseUsername);
            return false;
        }

        if (entry.code().equals(submittedCode)) {
            System.out.println("Email update verification code VALID for user: " + lowerCaseUsername);
            // Code is typically removed by the facade after successful use.
            return true;
        } else {
            System.out.println("INVALID email update verification code for user: " + lowerCaseUsername);
            return false;
        }
    }

    /**
     * Explicitly removes an email update verification code from its cache.
     * @param actorUsernameKey The username (lowercased) whose email update code should be removed.
     */
    public void removeEmailUpdateVerificationCode(String actorUsernameKey) {
        emailUpdateVerificationCache.remove(actorUsernameKey.toLowerCase());
        System.out.println("Explicitly removed email update verification data for user: " + actorUsernameKey.toLowerCase());
    }

    private void removeExpiredEntries() {
        Instant now = Instant.now();

        // Cleanup for main verification cache (registration, forgot password)
        int initialRegSize = verificationCache.size();
        verificationCache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().expiry().isBefore(now);
            if (expired) {
                System.out.println("Cache cleanup removing expired registration entry for username: " + entry.getKey());
            }
            return expired;
        });
        int finalRegSize = verificationCache.size();
        if (initialRegSize > 0 || finalRegSize < initialRegSize) {
            System.out.printf("Ran registration verification cache cleanup. Removed %d expired entries. Current cache size: %d%n", initialRegSize - finalRegSize, finalRegSize);
        }

        // Cleanup for email update verification cache
        int initialUpdateSize = emailUpdateVerificationCache.size();
        emailUpdateVerificationCache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().expiry().isBefore(now);
            if (expired) {
                System.out.println("Cache cleanup removing expired email update entry for username: " + entry.getKey());
            }
            return expired;
        });
        int finalUpdateSize = emailUpdateVerificationCache.size();
        if (initialUpdateSize > 0 || finalUpdateSize < initialUpdateSize) {
            System.out.printf("Ran email update verification cache cleanup. Removed %d expired entries. Current cache size: %d%n", initialUpdateSize - finalUpdateSize, finalUpdateSize);
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