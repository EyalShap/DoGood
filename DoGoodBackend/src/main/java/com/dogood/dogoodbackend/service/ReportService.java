package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    private ReportsFacade reportsFacade;

    @Autowired
    public ReportService(FacadeManager facadeManager){
        this.reportsFacade = facadeManager.getReportsFacade();
    }

    public Response<Integer> createReport(String token, String actor, int reportedPostId, String description) {
        //TODO: check token

        try {
            int reportId = reportsFacade.createReport(actor, reportedPostId, description);
            return Response.createResponse(reportId);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeReport(String token, int reportId, String actor) {
        //TODO: check token

        try {
            reportsFacade.removeReport(reportId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> editReport(String token, int reportId, String actor, String description) {
        //TODO: check token

        try {
            reportsFacade.editReport(reportId, actor, description);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ReportDTO> getReport(String token, int reportId, String actor) {
        //TODO: check token

        try {
            ReportDTO report = reportsFacade.getReport(reportId, actor);
            return Response.createResponse(report);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ReportDTO>> getAllReportDTOs(String token, String actor) {
        //TODO: check token

        try {
            List<ReportDTO> reports = reportsFacade.getAllReportDTOs(actor);
            return Response.createResponse(reports);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }
}
