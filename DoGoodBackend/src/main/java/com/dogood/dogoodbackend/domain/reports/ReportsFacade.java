package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.utils.ReportErrors;
import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Transactional
public class ReportsFacade {
    private ReportRepository reportRepository;
    //private UsersFacade usersFacade;
    private PostsFacade postsFacade;

    public ReportsFacade(ReportRepository reportRepository, PostsFacade postsFacade) {
        this.reportRepository = reportRepository;
        this.postsFacade = postsFacade;
    }

    public int createReport(String actor, int reportedPostId, String description) {
        //TODO: check if user exists and logged in

        if(!postsFacade.doesExist(reportedPostId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportedPostDoesNotExistError(reportedPostId));
        }

        return reportRepository.createReport(actor, reportedPostId, description).getId();
    }

    public void removeReport(int reportId, String actor) {
        //TODO: check if user exists and logged in

        Report toRemove = reportRepository.getReport(reportId);
        if(!toRemove.getReportingUser().equals(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(ReportErrors.makeUserUnauthorizedToMakeActionError(actor, reportId, "remove"));
        }

        reportRepository.removeReport(reportId);
    }

    public void editReport(int reportId, String actor, String description) {
        //TODO: check if user exists and logged in

        Report toEdit = reportRepository.getReport(reportId);
        if(!toEdit.getReportingUser().equals(actor)  && !isAdmin(actor)) {
            throw new IllegalArgumentException(ReportErrors.makeUserUnauthorizedToMakeActionError(actor, reportId, "edit"));
        }

        reportRepository.editReport(reportId, description);
    }

    public ReportDTO getReport(int reportId, String actor) {
        //TODO: check if user exists and logged in

        Report report = reportRepository.getReport(reportId);
        ReportDTO reportDTO = new ReportDTO(report);
        return reportDTO;
    }

    public List<ReportDTO> getAllReportDTOs(String actor) {
        //TODO: check if user exists and logged in

        if(!isAdmin(actor)) {
            throw new IllegalArgumentException(ReportErrors.makeUserTriedToViewReportsError(actor));
        }

        return reportRepository.getAllReportDTOs();
    }

    @Transactional
    public void removePostReports(int postId) {
        reportRepository.removePostReports(postId);
    }

    //TODO: change this when users facade is implemented
    private boolean isAdmin(String user) {
        return true;
    }
}
