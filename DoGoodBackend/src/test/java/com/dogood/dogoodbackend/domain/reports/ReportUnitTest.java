package com.dogood.dogoodbackend.domain.reports;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportUnitTest {
//    private Report report;
//    private final String id = "0";
    private final String reportingUser = "NotTheDoctor";
    private final String reportedId = "0";
    private final String description = "Offensive";

    @BeforeEach
    void setUp() {
//        this.report = new Report(reportingUser, description, reportedId, ReportObject.USER);
    }

    // Note: don't need to test edit/get

    @Test
    void givenValidFields_whenConstructor_thenSuccess() {
        String newDescription = "A new description";
        Assertions.assertDoesNotThrow(() -> new Report(reportingUser, newDescription, reportedId, ReportObject.USER));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"d", "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddescription"})
    void givenInvalidFields_whenConstructor_thenThrowException(String invalidDescription) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Report(reportingUser, invalidDescription, reportedId, ReportObject.USER);
        });
        assertEquals(String.format("Invalid report description: %s.", invalidDescription), exception.getMessage());
    }
}