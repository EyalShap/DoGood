package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.utils.ReportErrors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MemoryReportRepository implements ReportRepository{
    private Map<Integer, Report> reports;
    private int nextReportId;

    public MemoryReportRepository() {
        this.reports = new HashMap<>();
        this.nextReportId = 0;
    }

    @Override
    public Report createReport(int reportingUserId, int reportedPostId, String description) {
        if(reports.containsKey(nextReportId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportIdAlreadyExistsError(nextReportId));
        }

        Report newReport = new Report(nextReportId, reportingUserId, reportedPostId, description);

        // trying to prevent a vicious ddos attack????????????
        if(isDuplicateReport(newReport)) {
            throw new IllegalArgumentException(ReportErrors.makeReportContentAlreadyExistsError());
        }

        reports.put(nextReportId, newReport);
        nextReportId++;
        return newReport;
    }

    private boolean isDuplicateReport(Report newReport) {
        // assuming each user can report a specific post only once a day
        for(Report report : reports.values()) {
            if(newReport.getReportedPostId() == report.getReportedPostId() && newReport.getDescription().equals(report.getDescription()) && newReport.getDate().isEqual(report.getDate())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeReport(int reportId, int actorId) {
        if(!reports.containsKey(reportId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(reportId));
        }

        reports.remove(reportId);
    }

    @Override
    public void editReport(int reportId, int actorId, String description) {
        if(!reports.containsKey(reportId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(reportId));
        }

        reports.get(reportId).update(description);
    }

    @Override
    public Report getReport(int reportId) {
        if(!reports.containsKey(reportId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(reportId));
        }
        return reports.get(reportId);
    }

    @Override
    public List<Report> getAllReports() {
        return new ArrayList<>(reports.values());
    }
}
