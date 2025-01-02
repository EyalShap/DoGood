package com.dogood.dogoodbackend.domain.reports;

import java.time.LocalDate;

public class ReportDTO {
    private int id;
    private String reportingUser;
    private int reportedPostId;
    private String description;
    private LocalDate date;

    public ReportDTO() {}

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(String reportingUser) {
        this.reportingUser = reportingUser;
    }

    public int getReportedPostId() {
        return reportedPostId;
    }

    public void setReportedPostId(int reportedPostId) {
        this.reportedPostId = reportedPostId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
