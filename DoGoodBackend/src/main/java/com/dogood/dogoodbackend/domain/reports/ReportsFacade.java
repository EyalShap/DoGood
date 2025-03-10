package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.utils.ReportErrors;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public class ReportsFacade {
    private ReportRepository reportRepository;
    private UsersFacade usersFacade;
    private PostsFacade postsFacade;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;

    public ReportsFacade(UsersFacade usersFacade, ReportRepository reportRepository, PostsFacade postsFacade, VolunteeringFacade volunteeringFacade, OrganizationsFacade organizationsFacade) {
        this.usersFacade = usersFacade;
        this.reportRepository = reportRepository;
        this.postsFacade = postsFacade;
        this.volunteeringFacade = volunteeringFacade;
        this.organizationsFacade = organizationsFacade;
    }

    private ReportDTO createReport(String actor, String description, String reportedId, ReportObject reportObject) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        Report report = reportRepository.createReport(actor, reportedId, description, reportObject);
        return new ReportDTO(report);
    }

    public ReportDTO createVolunteeringPostReport(String actor, int reportedPostId, String description) {
        if(!postsFacade.doesVolunteeringPostExist(reportedPostId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportedPostDoesNotExistError(reportedPostId));
        }
        return createReport(actor, description, reportedPostId + "", ReportObject.VOLUNTEERING_POST);
    }

    public ReportDTO createVolunteerPostReport(String actor, int reportedPostId, String description) {
        if(!postsFacade.doesVolunteerPostExist(reportedPostId)) {
            throw new IllegalArgumentException(ReportErrors.makeReportedPostDoesNotExistError(reportedPostId));
        }
        return createReport(actor, description, reportedPostId + "", ReportObject.VOLUNTEER_POST);
    }

    public ReportDTO createVolunteeringReport(String actor, int reportedVolunteeringId, String description) {
        volunteeringFacade.getVolunteeringDTO(reportedVolunteeringId); // checks if exists
        return createReport(actor, description, reportedVolunteeringId + "", ReportObject.VOLUNTEERING);
    }

    public ReportDTO createOrganizationReport(String actor, int reportedOrganizationId, String description) {
        organizationsFacade.getOrganization(reportedOrganizationId); // checks if exists
        return createReport(actor, description, reportedOrganizationId + "", ReportObject.ORGANIZATION);
    }

    public ReportDTO createUserReport(String actor, String reportedUserId, String description) {
        usersFacade.getUser(reportedUserId); // checks if exists
        return createReport(actor, description, reportedUserId, ReportObject.USER);
    }

    private void removeReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        if(!reportingUser.equals(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(ReportErrors.makeUserUnauthorizedToMakeActionError(actor, "remove"));
        }

        reportRepository.removeReport(reportingUser, date, reportedId, reportObject);
    }

    public void removeVolunteeringPostReport(String reportingUser, LocalDate date, int reportedId, String actor) {
        removeReport(reportingUser, date, reportedId + "", ReportObject.VOLUNTEERING_POST, actor);
    }

    public void removeVolunteerPostReport(String reportingUser, LocalDate date, int reportedId, String actor) {
        removeReport(reportingUser, date, reportedId + "", ReportObject.VOLUNTEER_POST, actor);
    }

    public void removeVolunteeringReport(String reportingUser, LocalDate date, int reportedId, String actor) {
        removeReport(reportingUser, date, reportedId + "", ReportObject.VOLUNTEERING, actor);
    }

    public void removeOrganizationReport(String reportingUser, LocalDate date, int reportedId, String actor) {
        removeReport(reportingUser, date, reportedId + "", ReportObject.ORGANIZATION, actor);
    }

    public void removeUserReport(String reportingUser, LocalDate date, String reportedId, String actor) {
        removeReport(reportingUser, date, reportedId, ReportObject.USER, actor);
    }

    public void editReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject, String actor, String description) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }

        if(!reportingUser.equals(actor)  && !isAdmin(actor)) {
            throw new IllegalArgumentException(ReportErrors.makeUserUnauthorizedToMakeActionError(actor, "edit"));
        }

        reportRepository.editReport(reportingUser, date, reportedId, reportObject, description);
    }

    private ReportDTO getReport(String reportingUser, LocalDate date, String reportedId, ReportObject reportObject, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Report report = reportRepository.getReport(reportingUser, date, reportedId, reportObject);
        ReportDTO reportDTO = new ReportDTO(report);
        return reportDTO;
    }

    public ReportDTO getVolunteeringPostReport(String reportingUser, LocalDate date, int reportedId, String actor) {
        return getReport(reportingUser, date, reportedId + "", ReportObject.VOLUNTEERING_POST, actor);
    }

    public ReportDTO getVolunteerPostReport(String reportingUser, LocalDate date, int reportedId, String actor) {
        return getReport(reportingUser, date, reportedId + "", ReportObject.VOLUNTEER_POST, actor);
    }

    public ReportDTO getVolunteeringReport(String reportingUser, LocalDate date, int reportedId, String actor) {
        return getReport(reportingUser, date, reportedId + "", ReportObject.VOLUNTEERING, actor);
    }

    public ReportDTO getOrganizationReport(String reportingUser, LocalDate date, int reportedId, String actor) {
        return getReport(reportingUser, date, reportedId + "", ReportObject.ORGANIZATION, actor);
    }

    public ReportDTO getUserReport(String reportingUser, LocalDate date, String reportedId, String actor) {
        return getReport(reportingUser, date, reportedId, ReportObject.USER, actor);
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

    public void removeVolunteeringPostReports(int id) {
        reportRepository.removeObjectReports(id + "", ReportObject.VOLUNTEERING_POST);
    }

    public void removeVolunteerPostReports(int id) {
        reportRepository.removeObjectReports(id + "", ReportObject.VOLUNTEER_POST);
    }

    public void removeVolunteeringReports(int id) {
        reportRepository.removeObjectReports(id + "", ReportObject.VOLUNTEERING);
    }

    public void removeOrganizationReports(int id) {
        reportRepository.removeObjectReports(id + "", ReportObject.ORGANIZATION);
    }

    public void removeUserReports(String id) {
        reportRepository.removeObjectReports(id, ReportObject.USER);
    }

    private boolean isAdmin(String user) {
        return usersFacade.isAdmin(user);
    }
    private boolean userExists(String user){
        return usersFacade.userExists(user);
    }
}
