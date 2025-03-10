package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.posts.VolunteeringPost;
import com.dogood.dogoodbackend.jparepos.ReportJPA;
import com.dogood.dogoodbackend.utils.PostErrors;
import com.dogood.dogoodbackend.utils.ReportErrors;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DBReportRepository implements ReportRepository{
    private ReportJPA jpa;

    public DBReportRepository(ReportJPA jpa) {
        this.jpa = jpa;
    }

    public DBReportRepository() {}

    public void setJPA(ReportJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public Report createReport(String reportingUser, String reportedId, String description, ReportObject reportObject) {
        Report report = new Report(reportingUser, description, reportedId, reportObject);
        jpa.save(report);
        return report;
    }

    @Override
    public void removeReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject) {
        ReportKey key = new ReportKey(reportingUser, date, reportedId, reportObject);
        if(!jpa.existsById(key)) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(key));
        }
        jpa.deleteById(key);
    }

    @Override
    public void removeObjectReports(String reportedId, ReportObject reportObject) {
        jpa.deleteByReportedIdAndReportObject(reportedId, reportObject);
    }

    @Override
    public void editReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject, String description) {
        Report report = getReport(reportingUser, date, reportedId, reportObject);
        report.edit(description);
        jpa.save(report);
    }

    @Override
    public Report getReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject) {
        ReportKey key = new ReportKey(reportingUser, date, reportedId, reportObject);
        Optional<Report> report = jpa.findById(key);
        if(!report.isPresent()) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(key));
        }
        return report.get();
    }

    @Override
    public List<Report> getAllReports() {
        return jpa.findAll();
    }

    @Override
    public void clear() {
        jpa.deleteAll();
    }
}
