package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.reportquests.CreateReportRequest;
import com.dogood.dogoodbackend.api.reportquests.RemoveReportRequest;
import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.domain.reports.ReportObject;
import com.dogood.dogoodbackend.service.ReportService;
import com.dogood.dogoodbackend.service.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportAPI {
    @Autowired
    private ReportService reportService;

    @PostMapping("/createVolunteeringPostReport")
    public Response<ReportDTO> createVolunteeringPostReport(@RequestBody CreateReportRequest createReportRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = createReportRequest.getActor();
        int reportedId = createReportRequest.getReportedId();
        String description = createReportRequest.getDescription();
        return reportService.createVolunteeringPostReport(token, actor, reportedId, description);
    }

    @PostMapping("/createVolunteerPostReport")
    public Response<ReportDTO> createVolunteerPostReport(@RequestBody CreateReportRequest createReportRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = createReportRequest.getActor();
        int reportedId = createReportRequest.getReportedId();
        String description = createReportRequest.getDescription();
        return reportService.createVolunteerPostReport(token, actor, reportedId, description);
    }

    @PostMapping("/createVolunteeringReport")
    public Response<ReportDTO> createVolunteeringReport(@RequestBody CreateReportRequest createReportRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = createReportRequest.getActor();
        int reportedId = createReportRequest.getReportedId();
        String description = createReportRequest.getDescription();
        return reportService.createVolunteeringReport(token, actor, reportedId, description);
    }

    @PostMapping("/createOrganizationReport")
    public Response<ReportDTO> createOrganizationReport(@RequestBody CreateReportRequest createReportRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = createReportRequest.getActor();
        int reportedId = createReportRequest.getReportedId();
        String description = createReportRequest.getDescription();
        return reportService.createOrganizationReport(token, actor, reportedId, description);
    }

    @PostMapping("/createUserReport")
    public Response<ReportDTO> createUserReport(@RequestParam String actor, @RequestParam String reportedId, @RequestParam String description, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.createUserReport(token, actor, reportedId, description);
    }

    @DeleteMapping("/removeVolunteeringPostReport")
    public Response<Boolean> removeVolunteeringPostReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam int reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        return reportService.removeVolunteeringPostReport(token, reportingUser, date, reportedId, actor);
    }

    @DeleteMapping("/removeVolunteerPostReport")
    public Response<Boolean> removeVolunteerPostReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam int reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        return reportService.removeVolunteerPostReport(token, reportingUser, date, reportedId, actor);
    }

    @DeleteMapping("/removeVolunteeringReport")
    public Response<Boolean> removeVolunteeringReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam int reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        return reportService.removeVolunteeringReport(token, reportingUser, date, reportedId, actor);
    }

    @DeleteMapping("/removeOrganizationReport")
    public Response<Boolean> removeOrganizationReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam int reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        return reportService.removeOrganizationReport(token, reportingUser, date, reportedId, actor);
    }

    @DeleteMapping("/removeUserReport")
    public Response<Boolean> removeUserReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam String reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.removeUserReport(token, reportingUser, date, reportedId, actor);
    }

    @PutMapping("/editReport")
    public Response<Boolean> editReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam String reportedId, @RequestParam ReportObject reportObject, @RequestParam String actor, @RequestParam String description, HttpServletRequest request) {
        String token = getToken(request);
        ;
        return reportService.editReport(token, reportingUser, date, reportedId, reportObject, actor, description);
    }

    @GetMapping("/getVolunteeringPostReport")
    public Response<ReportDTO> getVolunteeringPostReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam int reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getVolunteeringPostReport(token, reportingUser, date, reportedId, actor);
    }

    @GetMapping("/getVolunteerPostReport")
    public Response<ReportDTO> getVolunteerPostReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam int reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getVolunteerPostReport(token, reportingUser, date, reportedId, actor);
    }

    @GetMapping("/getVolunteeringReport")
    public Response<ReportDTO> getVolunteeringReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam int reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getVolunteeringReport(token, reportingUser, date, reportedId, actor);
    }

    @GetMapping("/getOrganizationReport")
    public Response<ReportDTO> getOrganizationReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam int reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getOrganizationReport(token, reportingUser, date, reportedId, actor);
    }

    @GetMapping("/getUserReport")
    public Response<ReportDTO> getUserReport(@RequestParam String reportingUser, @RequestParam LocalDate date, @RequestParam String reportedId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getUserReport(token, reportingUser, date, reportedId, actor);
    }

    @GetMapping("/getAllReports")
    public Response<List<ReportDTO>> getAllReportDTOs(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getAllReportDTOs(token, actor);
    }

    @GetMapping("/getAllVolunteeringPostReports")
    public Response<List<ReportDTO>> getAllVolunteeringPostReports(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getAllVolunteeringPostReports(token, actor);
    }

    @GetMapping("/getAllVolunteerPostReports")
    public Response<List<ReportDTO>> getAllVolunteerPostReports(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getAllVolunteerPostReports(token, actor);
    }

    @GetMapping("/getAllVolunteeringReports")
    public Response<List<ReportDTO>> getAllVolunteeringReports(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getAllVolunteeringReports(token, actor);
    }

    @GetMapping("/getAllUserReports")
    public Response<List<ReportDTO>> getAllUserReports(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getAllUserReports(token, actor);
    }

    @GetMapping("/getAllOrganizationReports")
    public Response<List<ReportDTO>> getAllOrganizationReports(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getAllOrganizationReports(token, actor);
    }

    @PostMapping("/banEmail")
    public Response<Boolean> banEmail(@RequestParam String actor, @RequestBody String email, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.banEmail(token, actor, email);
    }

    @DeleteMapping("/unbanEmail")
    public Response<Boolean> unbanEmail(@RequestParam String actor, @RequestParam String email, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.unbanEmail(token, actor, email);
    }

    @GetMapping("/getBannedEmails")
    public Response<List<String>> getBannedEmails(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getBannedEmails(token, actor);
    }
}
