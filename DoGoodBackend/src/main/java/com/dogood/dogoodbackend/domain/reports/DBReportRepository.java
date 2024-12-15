package com.dogood.dogoodbackend.domain.reports;

import java.time.LocalDate;
import java.util.List;

public class DBReportRepository implements ReportRepository{
    @Override
    public Report createReport(int reportingUserId, int reportedPostId, String description) {
        //TODO
        return null;
    }

    @Override
    public void removeReport(int reportId, int actorId) {
        //TODO
    }

    @Override
    public void editReport(int reportId, int actorId, String description) {
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
