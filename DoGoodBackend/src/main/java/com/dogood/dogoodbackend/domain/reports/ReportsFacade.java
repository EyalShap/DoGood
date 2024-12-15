package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.utils.ReportErrors;

import java.time.LocalDate;
import java.util.List;

public class ReportsFacade {
    private ReportRepository reportRepository;
    private UsersFacade usersFacade;
    private PostsFacade postsFacade;

    public ReportsFacade(ReportRepository reportRepository, UsersFacade usersFacade, PostsFacade postsFacade) {
        this.reportRepository = reportRepository;
        this.usersFacade = usersFacade;
        this.postsFacade = postsFacade;
    }

    public void createReport(int reportingUserId, int reportedPostId, String description) {
        if(!postsFacade.doesExist(reportedPostId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportedPostDoesNotExistError(reportedPostId));
        }

        reportRepository.createReport(reportingUserId, reportedPostId, description);
    }

    public void removeReport(int reportId, int actorId) {
        // checking if the user trying to remove the report is the one created it / admin
        Report toRemove = reportRepository.getReport(reportId);
        if(toRemove.getReportingUserId() != actorId && !usersFacade.isAdmin(actorId)) {
            throw new IllegalArgumentException(ReportErrors.makeUserUnauthorizedToMakeActionError(actorId, reportId, "remove"));
        }

        reportRepository.removeReport(reportId, actorId);
    }

    public void editReport(int reportId, int actorId, String description) {
        // checking if the user trying to edit the report is the one created it / admin
        Report toEdit = reportRepository.getReport(reportId);
        if(toEdit.getReportingUserId() != actorId  && !usersFacade.isAdmin(actorId)) {
            throw new IllegalArgumentException(ReportErrors.makeUserUnauthorizedToMakeActionError(actorId, reportId, "edit"));
        }

        reportRepository.editReport(reportId, actorId, description);
    }

    public Report getReport(int reportId) {
        return reportRepository.getReport(reportId);
    }

    public List<Report> getAllReports(int actorId) {
        if(!usersFacade.isAdmin(actorId)) {
            throw new IllegalArgumentException(ReportErrors.makeUserTriedToViewReportsError(actorId));
        }

        return reportRepository.getAllReports();
    }
}
