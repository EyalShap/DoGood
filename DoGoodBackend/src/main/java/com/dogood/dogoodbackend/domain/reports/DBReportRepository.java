package com.dogood.dogoodbackend.domain.reports;

import java.time.LocalDate;
import java.util.List;

public class DBReportRepository implements ReportRepository{
    @Override
    public Report createReport(String reportingUser, int reportedPostId, String description) {
        //TODO
        return null;
    }

    @Override
    public void removeReport(int reportId) {
        //TODO
    }

    @Override
    public void editReport(int reportId, String description) {
        //TODO
    }

    @Override
    public Report getReport(int reportId) {
        //TODO
        return null;
    }

    @Override
    public List<Report> getAllReports() {
        //TODO
        return null;
    }
}
