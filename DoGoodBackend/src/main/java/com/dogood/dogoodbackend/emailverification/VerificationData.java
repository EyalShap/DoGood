package com.dogood.dogoodbackend.emailverification;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import java.time.Instant;

// This record is used by VerificationCacheService for initial registration verification data.
// It's keyed by username in the cache.
public record VerificationData(
        String code,
        Instant expiry,
        RegisterRequest userData // Contains user details, password should be hashed by caller
) {}