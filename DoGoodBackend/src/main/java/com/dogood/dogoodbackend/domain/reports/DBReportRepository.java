package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.posts.VolunteeringPost;
import com.dogood.dogoodbackend.jparepos.ReportJPA;
import com.dogood.dogoodbackend.utils.PostErrors;
import com.dogood.dogoodbackend.utils.ReportErrors;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DBReportRepository implements ReportRepository{
    private ReportJPA jpa;

    public DBReportRepository(ReportJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public Report createReport(String reportingUser, int reportedPostId, String description) {
        Report report = new Report(reportingUser, reportedPostId, description);
        jpa.save(report);
        return report;
    }

    @Override
    public void removeReport(int reportId) {
        if(!jpa.existsById(reportId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(reportId));
        }
        jpa.deleteById(reportId);
    }

    @Override
    @Transactional
    public void removePostReports(int postId) {
        jpa.deleteByReportedPostId(postId);
    }

    @Override
    public void editReport(int reportId, String description) {
        Report toEdit = getReport(reportId);
        toEdit.edit(description);
        jpa.save(toEdit);
    }

    @Override
    public Report getReport(int reportId) {
        Optional<Report> report = jpa.findById(reportId);
        if(!report.isPresent()) {
            throw new IllegalArgumentException(ReportErrors.makeReportDoesNotExistError(reportId));
        }

        return report.get();
    }

    @Override
    public List<Report> getAllReports() {
        return jpa.findAll();
    }
}
