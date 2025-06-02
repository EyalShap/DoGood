package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.requests.RequestObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportKey reportKey = (ReportKey) o;
        return Objects.equals(reportingUser, reportKey.reportingUser) && Objects.equals(date, reportKey.date) && Objects.equals(reportedId, reportKey.reportedId) && reportObject == reportKey.reportObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportingUser, date, reportedId, reportObject);
    }
}
