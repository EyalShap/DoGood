package com.dogood.dogoodbackend.domain.reports;

import java.time.LocalDate;
import java.util.Objects;

public class Report {
    private int id;
    private int reportingUserId;
    private int reportedPostId;
    private String description;
    private LocalDate date;

    public Report(int id, int reportingUserId, int reportedPostId, String description) {
        this.id = id;
        this.reportingUserId = reportingUserId;
        this.reportedPostId = reportedPostId;
        this.description = description;
        this.date = LocalDate.now();
    }

    public void update(String description) {
        this.description = description;
        this.date = LocalDate.now();
    }

    public int getId() {
        return id;
    }

    public int getReportingUserId() {
        return reportingUserId;
    }

    public int getReportedPostId() {
        return reportedPostId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return id == report.id && reportingUserId == report.reportingUserId && reportedPostId == report.reportedPostId && Objects.equals(description, report.description) && Objects.equals(date, report.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportingUserId, reportedPostId, description, date);
    }
}
