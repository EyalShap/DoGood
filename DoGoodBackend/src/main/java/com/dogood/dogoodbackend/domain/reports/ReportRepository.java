package com.dogood.dogoodbackend.domain.reports;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public interface ReportRepository {
    public Report createReport(String reportingUser, String reportedId, String description, ReportObject reportObject);
    public void removeReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject);
    public void removeObjectReports(String reportedId, ReportObject reportObject);
    public void editReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject, String description);
    public Report getReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject);
    public List<Report> getAllReports();
    public List<Report> getAllVolunteeringPostReports();
    public List<Report> getAllVolunteerPostReports();
    public List<Report> getAllVolunteeringReports();
    public List<Report> getAllUserReports();
    public List<Report> getAllOrganizationReports();
    public void clear();

    public default List<ReportDTO> getAllReportDTOs() {
        return getReportDTOs(getAllReports());
    }

    public default List<ReportDTO> getReportDTOs(List<Report> reports) {
        List<ReportDTO> reportDTOS = reports.stream()
                .map(report -> new ReportDTO(report))
                .collect(Collectors.toList());
        return reportDTOS;
    }
}
