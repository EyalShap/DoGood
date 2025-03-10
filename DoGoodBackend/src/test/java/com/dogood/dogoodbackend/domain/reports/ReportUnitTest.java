package com.dogood.dogoodbackend.domain.reports;

import net.bytebuddy.implementation.bind.annotation.Empty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportUnitTest {
    private Report report;
    private final int id = 0;
    private final String reportingUser = "NotTheDoctor";
    private final int reportedPostId = 0;
    private final String description = "Offensive";

    @BeforeEach
    void setUp() {
        //this.report = new VolunteeringPostReport(id, reportingUser, description, reportedPostId);
    }

    @Test
    void givenValidFields_whenEdit_thenEdit() {
        String newDescription = "A new description";
        this.report.edit(newDescription);
        //Report edited = new VolunteeringPostReport(id, reportingUser, newDescription, reportedPostId);
        //assertEquals(edited, this.report);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"d", "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddescription"})
    void givenInvalidFields_whenEdit_thenThrowException(String invalidDescription) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            this.report.edit(invalidDescription);
        });
        assertEquals(String.format("Invalid report description: %s.", invalidDescription), exception.getMessage());

        //Report notEdited = new VolunteeringPostReport(id, reportingUser, description, reportedPostId);
        //assertEquals(notEdited, this.report);
    }
}