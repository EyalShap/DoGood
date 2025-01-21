package com.dogood.dogoodbackend.domain.reports;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

import static com.dogood.dogoodbackend.utils.ValidateFields.*;

@Entity
@Table(name = "posts_report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private int id;

    @Column(name = "reporting_user")
    private String reportingUser;

    @Column(name = "reported_post_id")
    private int reportedPostId;

    @Column(name = "report_description")
    private String description;

    @Column(name = "report_date")
    private LocalDate date;

    public Report(int id, String reportingUser, int reportedPostId, String description) {
        this.id = id;
        setFields(reportingUser, reportedPostId, description);
    }

    public Report(String reportingUser, int reportedPostId, String description) {
        setFields(reportingUser, reportedPostId, description);
    }

    private void setFields(String reportingUser, int reportedPostId, String description) {
        String isValidOrg = isValid(description);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.reportingUser = reportingUser;
        this.reportedPostId = reportedPostId;
        this.description = description;
        this.date = LocalDate.now();
    }

    public Report() {}

    private String isValid(String description) {
        StringBuilder res = new StringBuilder();
        if(!isValidText(description, 2,100)) {
            res.append(String.format("Invalid report description: %s.", description));
        }
        return res.toString();
    }

    public void edit(String description) {
        String isValidOrg = isValid(description);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.description = description;
        this.date = LocalDate.now();
    }

    public int getId() {
        return id;
    }

    public String getReportingUser() {
        return reportingUser;
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
        return id == report.id && reportedPostId == report.reportedPostId && Objects.equals(reportingUser, report.reportingUser) && Objects.equals(description, report.description) && Objects.equals(date, report.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportingUser, reportedPostId, description, date);
    }
}
