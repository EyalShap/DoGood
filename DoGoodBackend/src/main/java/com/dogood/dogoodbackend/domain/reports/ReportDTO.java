package com.dogood.dogoodbackend.domain.reports;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.Objects;

public class ReportDTO {
    private String reportingUser;
    private String description;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate date;
    private String reportedId;
    private ReportObject reportObject;

    public ReportDTO() {}

    public ReportDTO(String reportingUser, String description, LocalDate date, String reportedId, ReportObject reportObject) {
        this.reportingUser = reportingUser;
        this.description = description;
        this.date = date;
        this.reportedId = reportedId;
        this.reportObject = reportObject;
    }

    public ReportDTO(Report report) {
        this.reportingUser = report.getReportingUser();
        this.description = report.getDescription();
        this.date = report.getDate();
        this.reportedId = report.getReportedId();
        this.reportObject = report.getReportObject();
    }

    public String getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(String reportingUser) {
        this.reportingUser = reportingUser;
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

    public String getReportedId() {
        return reportedId;
    }

    public void setReportedId(String reportedId) {
        this.reportedId = reportedId;
    }

    public ReportObject getReportObject() {
        return reportObject;
    }

    public void setReportObject(ReportObject reportObject) {
        this.reportObject = reportObject;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportDTO report = (ReportDTO) o;
        return Objects.equals(reportingUser, report.reportingUser) && Objects.equals(description, report.description) && Objects.equals(date, report.date) && Objects.equals(reportedId, report.reportedId) && reportObject == report.reportObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportingUser, description, date, reportedId, reportObject);
    }
}
