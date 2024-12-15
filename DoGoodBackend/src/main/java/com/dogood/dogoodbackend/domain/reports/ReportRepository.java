package com.dogood.dogoodbackend.domain.reports;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository {
    public Report createReport(int reportingUserId, int reportedPostId, String description);
    public void removeReport(int reportId, int actorId);
    public void editReport(int reportId, int actorId, String description);
    public Report getReport(int reportId);
    public List<Report> getAllReports();
}
