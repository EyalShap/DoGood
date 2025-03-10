package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.requests.RequestObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class ReportKey {
    private String reportingUser;
    private LocalDate date;
    private String reportedId;
    private ReportObject reportObject;

    public ReportKey(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject) {
        this.reportingUser = reportingUser;
        this.date = date;
        this.reportedId = reportedId;
        this.reportObject = reportObject;
    }

    public ReportKey() {}

    public String getReportingUser() {
        return reportingUser;
    }

    public void setReportingUser(String reportingUser) {
        this.reportingUser = reportingUser;
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
}
