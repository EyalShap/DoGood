package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    private ReportsFacade reportsFacade;
    private AuthFacade authFacade;

    @Autowired
    public ReportService(FacadeManager facadeManager){
        this.reportsFacade = facadeManager.getReportsFacade();
        this.authFacade = facadeManager.getAuthFacade();
    }

    private void checkToken(String token, String username){
        if(!authFacade.getNameFromToken(token).equals(username)){
            throw new IllegalArgumentException("Invalid token");
        }
    }
    public Response<Integer> createReport(String token, String actor, int reportedPostId, String description) {
        try {
            checkToken(token, actor);
            int reportId = reportsFacade.createReport(actor, reportedPostId, description);
            return Response.createResponse(reportId);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeReport(String token, int reportId, String actor) {
        try {
            checkToken(token, actor);
            reportsFacade.removeReport(reportId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> editReport(String token, int reportId, String actor, String description) {
        try {
            checkToken(token, actor);
            reportsFacade.editReport(reportId, actor, description);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> getReport(String token, int reportId, String actor) {
        try {
            checkToken(token, actor);
            ReportDTO report = reportsFacade.getReport(reportId, actor);
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
}
