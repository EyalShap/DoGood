package com.dogood.dogoodbackend.emailverification;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import java.time.Instant;

// Ensure this is a top-level public record
public record VerificationData(
        String code,
        Instant expiry,
        RegisterRequest userData // Contains raw password, User constructor will hash it
) {}