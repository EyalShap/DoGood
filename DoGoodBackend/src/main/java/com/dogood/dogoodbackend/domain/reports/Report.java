package com.dogood.dogoodbackend.domain.reports;

import java.time.LocalDate;
import java.util.Objects;

import static com.dogood.dogoodbackend.utils.ValidateFields.*;

public class Report {
    private int id;
    private String reportingUser;
    private int reportedPostId;
    private String description;
    private LocalDate date;

    public Report(int id, String reportingUser, int reportedPostId, String description) {
        String isValidOrg = isValid(id, description);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.id = id;
        this.reportingUser = reportingUser;
        this.reportedPostId = reportedPostId;
        this.description = description;
        this.date = LocalDate.now();
    }

    private String isValid(int id, String description) {
        StringBuilder res = new StringBuilder();
        if(id < 0) {
            res.append(String.format("Invalid id: %d.\n", id));
        }
        if(!isValidText(description, 2,100)) {
            res.append(String.format("Invalid report description: %s.", description));
        }
        return res.toString();
    }

    public void edit(String description) {
        String isValidOrg = isValid(id, description);
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
