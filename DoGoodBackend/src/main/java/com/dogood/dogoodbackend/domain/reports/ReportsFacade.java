package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.utils.ReportErrors;
import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Transactional
public class ReportsFacade {
    private ReportRepository reportRepository;
    private UsersFacade usersFacade;
    private PostsFacade postsFacade;

    public ReportsFacade(UsersFacade usersFacade, ReportRepository reportRepository, PostsFacade postsFacade) {
        this.usersFacade = usersFacade;
        this.reportRepository = reportRepository;
        this.postsFacade = postsFacade;
    }

    public int createReport(String actor, int reportedPostId, String description) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        if(!postsFacade.doesExist(reportedPostId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportedPostDoesNotExistError(reportedPostId));
        }

        return reportRepository.createReport(actor, reportedPostId, description);
    }

    public void removeReport(int reportId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        Report toRemove = reportRepository.getReport(reportId);
        if(!toRemove.getReportingUser().equals(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(ReportErrors.makeUserUnauthorizedToMakeActionError(actor, reportId, "remove"));
        }

        reportRepository.removeReport(reportId);
    }

    public void editReport(int reportId, String actor, String description) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        Report toEdit = reportRepository.getReport(reportId);
        if(!toEdit.getReportingUser().equals(actor)  && !isAdmin(actor)) {
            throw new IllegalArgumentException(ReportErrors.makeUserUnauthorizedToMakeActionError(actor, reportId, "edit"));
        }

        reportRepository.editReport(reportId, description);
    }

    public ReportDTO getReport(int reportId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Report report = reportRepository.getReport(reportId);
        ReportDTO reportDTO = new ReportDTO(report);
        return reportDTO;
    }

    public List<ReportDTO> getAllReportDTOs(String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        if(!isAdmin(actor)) {
            throw new IllegalArgumentException(ReportErrors.makeUserTriedToViewReportsError(actor));
        }

        return reportRepository.getAllReportDTOs();
    }

    @Transactional
    public void removePostReports(int postId) {
        reportRepository.removePostReports(postId);
    }

    private boolean isAdmin(String user) {
        return usersFacade.isAdmin(user);
    }
    private boolean userExists(String user){
        return usersFacade.userExists(user);
    }
}
