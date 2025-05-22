package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.domain.reports.ReportObject;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ReportService {
    private ReportsFacade reportsFacade;
    private AuthFacade authFacade;
    private UsersFacade usersFacade;

    @Autowired
    public ReportService(FacadeManager facadeManager){
        this.reportsFacade = facadeManager.getReportsFacade();
        this.authFacade = facadeManager.getAuthFacade();
        this.usersFacade = facadeManager.getUsersFacade();
    }

    private void checkToken(String token, String username){
        if(!authFacade.getNameFromToken(token).equals(username)){
            throw new IllegalArgumentException("Invalid token");
        }
        if (usersFacade.isBanned(username)) {
            throw new IllegalArgumentException("Banned user.");
        }
    }

    public Response<ReportDTO> createVolunteeringPostReport(String token, String actor, int reportedPostId, String description) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.createVolunteeringPostReport(actor, reportedPostId, description);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> createVolunteerPostReport(String token, String actor, int reportedPostId, String description) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.createVolunteerPostReport(actor, reportedPostId, description);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> createVolunteeringReport(String token, String actor, int reportedVolunteeringId, String description) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.createVolunteeringReport(actor, reportedVolunteeringId, description);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> createOrganizationReport(String token, String actor, int reportedOrganizationId, String description) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.createOrganizationReport(actor, reportedOrganizationId, description);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> createUserReport(String token, String actor, String reportedUserId, String description) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.createUserReport(actor, reportedUserId, description);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeVolunteeringPostReport(String token, String reportingUser, LocalDate date, int reportedId, String actor) {
        try {
            checkToken(token, actor);
            reportsFacade.removeVolunteeringPostReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeVolunteerPostReport(String token, String reportingUser, LocalDate date, int reportedId, String actor) {
        try {
            checkToken(token, actor);
            reportsFacade.removeVolunteerPostReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeVolunteeringReport(String token, String reportingUser, LocalDate date, int reportedId, String actor) {
        try {
            checkToken(token, actor);
            reportsFacade.removeVolunteeringReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeOrganizationReport(String token, String reportingUser, LocalDate date, int reportedId, String actor) {
        try {
            checkToken(token, actor);
            reportsFacade.removeOrganizationReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeUserReport(String token, String reportingUser, LocalDate date, String reportedId, String actor) {
        try {
            checkToken(token, actor);
            reportsFacade.removeUserReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> editReport(String token, String reportingUser, LocalDate date, String reportedId, ReportObject reportObject, String actor, String description) {
        try {
            checkToken(token, actor);
            reportsFacade.editReport(reportingUser, date, reportedId, reportObject, actor, description);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> getVolunteeringPostReport(String token, String reportingUser, LocalDate date, int reportedId, String actor) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.getVolunteeringPostReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> getVolunteerPostReport(String token, String reportingUser, LocalDate date, int reportedId, String actor) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.getVolunteerPostReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> getVolunteeringReport(String token, String reportingUser, LocalDate date, int reportedId, String actor) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.getVolunteeringReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> getOrganizationReport(String token, String reportingUser, LocalDate date, int reportedId, String actor) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.getOrganizationReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> getUserReport(String token, String reportingUser, LocalDate date, String reportedId, String actor) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.getUserReport(reportingUser, date, reportedId, actor);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ReportDTO>> getAllReportDTOs(String token, String actor) {
        try {
            checkToken(token, actor);
            List<ReportDTO> reports = reportsFacade.getAllReportDTOs(actor);
            return Response.createResponse(reports);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ReportDTO>> getAllVolunteeringPostReports(String token, String actor) {
        try {
            checkToken(token, actor);
            List<ReportDTO> reports = reportsFacade.getAllVolunteeringPostReports(actor);
            return Response.createResponse(reports);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ReportDTO>> getAllVolunteerPostReports(String token, String actor) {
        try {
            checkToken(token, actor);
            List<ReportDTO> reports = reportsFacade.getAllVolunteerPostReports(actor);
            return Response.createResponse(reports);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ReportDTO>> getAllVolunteeringReports(String token, String actor) {
        try {
            checkToken(token, actor);
            List<ReportDTO> reports = reportsFacade.getAllVolunteeringReports(actor);
            return Response.createResponse(reports);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ReportDTO>> getAllUserReports(String token, String actor) {
        try {
            checkToken(token, actor);
            List<ReportDTO> reports = reportsFacade.getAllUserReports(actor);
            return Response.createResponse(reports);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ReportDTO>> getAllOrganizationReports(String token, String actor) {
        try {
            checkToken(token, actor);
            List<ReportDTO> reports = reportsFacade.getAllOrganizationReports(actor);
            return Response.createResponse(reports);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> banEmail(String token, String actor, String email) {
        try {
            checkToken(token, actor);
            reportsFacade.banEmail(email, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> unbanEmail(String token, String actor, String email) {
        try {
            checkToken(token, actor);
            reportsFacade.unbanEmail(email, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getBannedEmails(String token, String actor) {
        try {
            checkToken(token, actor);
            List<String> res = reportsFacade.getBannedEmails();
            return Response.createResponse(res);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }
}
