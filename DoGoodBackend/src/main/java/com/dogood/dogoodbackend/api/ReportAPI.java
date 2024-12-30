package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.service.PostService;
import com.dogood.dogoodbackend.service.ReportService;
import com.dogood.dogoodbackend.service.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
@RequestMapping("/api/reports")
public class ReportAPI {
    @Autowired
    private ReportService reportService;

    @PostMapping("/createReport")
    public Response<Integer> createReport(@RequestBody CreateReportRequest createReportRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = createReportRequest.getActor();
        int reportedPostId = createReportRequest.getReportedPostId();
        String description = createReportRequest.getDescription();
        return reportService.createReport(token, actor, reportedPostId, description);
    }

    @DeleteMapping("/removeReport")
    public Response<Boolean> removeReport(@RequestBody GeneralRequest removeReportRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = removeReportRequest.getActor();
        int reportId = removeReportRequest.getId();
        return reportService.removeReport(token, reportId, actor);
    }

    @PutMapping("/editReport")
    public Response<Boolean> editReport(@RequestParam int reportId, @RequestBody CreateReportRequest editReportRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = editReportRequest.getActor();
        String description = editReportRequest.getDescription();
        return reportService.editReport(token, reportId, actor, description);
    }

    @GetMapping("/getReport")
    public Response<ReportDTO> getReport(@RequestParam int reportId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getReport(token, reportId, actor);
    }

    @GetMapping("/getAllReportDTOs")
    public Response<List<ReportDTO>> getAllReportDTOs(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return reportService.getAllReportDTOs(token, actor);
    }
}
