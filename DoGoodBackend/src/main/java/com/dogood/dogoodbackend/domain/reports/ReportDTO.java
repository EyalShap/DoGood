package com.dogood.dogoodbackend.domain.reports;

import java.time.LocalDate;

public class ReportDTO {
    private int id;
    private String reportingUser;
    private int reportedPostId;
    private String description;
    private LocalDate date;

    public ReportDTO(int id, String reportingUser, int reportedPostId, String description, LocalDate date) {
        this.id = id;
        this.reportingUser = reportingUser;
        this.reportedPostId = reportedPostId;
        this.description = description;
        this.date = date;
    }

    public ReportDTO(Report report) {
        this.id = report.getId();
        this.reportingUser = report.getReportingUser();
        this.reportedPostId = report.getReportedPostId();
        this.description = report.getDescription();
        this.date = report.getDate();
    }
}
