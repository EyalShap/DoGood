package com.dogood.dogoodbackend.domain.reports;
import java.util.List;
import java.util.stream.Collectors;

public interface ReportRepository {
    public Report createReport(String reportingUser, int reportedPostId, String description);
    public void removeReport(int reportId);
    public void removePostReports(int postId);
    public void editReport(int reportId, String description);
    public Report getReport(int reportId);
    public List<Report> getAllReports();

    public default List<ReportDTO> getAllReportDTOs() {
        List<Report> reports = getAllReports();
        List<ReportDTO> reportDTOS = reports.stream()
                .map(report -> new ReportDTO(report))
                .collect(Collectors.toList());
        return reportDTOS;
    }
}
