package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.utils.ReportErrors;

import java.time.LocalDate;
import java.util.*;


public class MemoryReportRepository implements ReportRepository{
    private Map<ReportKey, Report> reports;

    public MemoryReportRepository() {
        this.reports = new HashMap<>();
    }

    @Override
    public Report createReport(String reportingUser, String description, String reportedId, ReportObject reportObject) {
        Report newReport = new Report(reportingUser, description, reportedId, reportObject);
        ReportKey reportKey = new ReportKey(reportingUser, newReport.getDate(), reportedId, reportObject);

        if(reports.containsKey(reportKey)) {
            throw new IllegalArgumentException(ReportErrors.makeReportAlreadyExistsError(reportKey));
        }

        reports.put(reportKey, newReport);
        return newReport;
    }

    @Override
    public void removeReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject) {
        ReportKey reportKey = new ReportKey(reportingUser, date, reportedId, reportObject);
        if(!reports.containsKey(reportKey)) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(reportKey));
        }
        reports.remove(reportKey);
    }

    @Override
    public void removeObjectReports(String reportedId, ReportObject reportObject) {
        Set<ReportKey> reportsKeys = reports.keySet();
        for(ReportKey key : reportsKeys) {
            Report report = reports.get(key);
            if(report.getReportObject() == reportObject && report.getReportedId().equals(reportedId)) {
                reports.remove(key);
            }
        }
    }

    @Override
    public void editReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject, String description) {
        ReportKey reportKey = new ReportKey(reportingUser, date, reportedId, reportObject);
        if(!reports.containsKey(reportKey)) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(reportKey));
        }
        reports.get(reportKey).edit(description);
    }

    @Override
    public Report getReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject) {
        ReportKey reportKey = new ReportKey(reportingUser, date, reportedId, reportObject);
        if(!reports.containsKey(reportKey)) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(reportKey));
        }
        return reports.get(reportKey);
    }

    @Override
    public List<Report> getAllReports() {
        return new ArrayList<>(reports.values());
    }

    @Override
    public void clear() {
        reports = new HashMap<>();
    }
}
