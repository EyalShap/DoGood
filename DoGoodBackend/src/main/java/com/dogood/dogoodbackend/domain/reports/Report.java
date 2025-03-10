package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.requests.RequestKey;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

import static com.dogood.dogoodbackend.utils.ValidateFields.*;

@Entity
@Table(name = "reports")
@IdClass(ReportKey.class)
public class Report {
    @Id
    @Column(name = "reporting_user")
    private String reportingUser;

    @Column(name = "report_description")
    private String description;

    @Id
    @Column(name = "report_date")
    private LocalDate date;

    @Id
    @Column(name = "reported_id")
    private String reportedId;

    @Id
    @Column(name = "reported_object")
    private ReportObject reportObject;

    public Report(String reportingUser, String description, String reportedId, ReportObject reportObject) {
        setFields(reportingUser, description, reportedId, reportObject);
    }

    private void setFields(String reportingUser, String description, String reportedId, ReportObject reportObject) {
        String isValidOrg = isValid(description);
        if (isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.reportingUser = reportingUser;
        this.description = description;
        this.date = LocalDate.now();
        this.reportedId = reportedId;
        this.reportObject = reportObject;
    }

    public Report() {
    }

    private String isValid(String description) {
        StringBuilder res = new StringBuilder();
        if (!isValidText(description, 2, 100)) {
            res.append(String.format("Invalid report description: %s.", description));
        }
        return res.toString();
    }

    public void edit(String description) {
        String isValidOrg = isValid(description);
        if (isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.description = description;
        this.date = LocalDate.now();
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
        Report report = (Report) o;
        return Objects.equals(reportingUser, report.reportingUser) && Objects.equals(description, report.description) && Objects.equals(date, report.date) && Objects.equals(reportedId, report.reportedId) && reportObject == report.reportObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportingUser, description, date, reportedId, reportObject);
    }
}
