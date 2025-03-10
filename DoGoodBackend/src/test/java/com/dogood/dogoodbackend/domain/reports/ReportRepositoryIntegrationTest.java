package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.jparepos.ReportJPA;
import com.dogood.dogoodbackend.utils.ReportErrors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
class ReportRepositoryIntegrationTest {
    private static MemoryReportRepository memoryReportRepository;
    private static DBReportRepository dbReportRepository;
    private int memId, dbId, id;
    private Report memReport, dbReport;
    private final int reportedPostId = 0;
    private final String description = "Offensive";
    private final String actor = "NotTheDoctor";

    @Autowired
    private ApplicationContext applicationContext;
    private ReportJPA reportJPA;

    @BeforeAll
    static void setUpBeforeAll() {
        memoryReportRepository = new MemoryReportRepository();
        dbReportRepository = new DBReportRepository();
    }

    @BeforeEach
    void setUpBeforeEach() {
        ReportJPA reportJPA = applicationContext.getBean(ReportJPA.class);
        dbReportRepository.setJPA(reportJPA);
        reportJPA.deleteAll();

        //memId = memoryReportRepository.createVolunteeringPostReport(actor, reportedPostId, description);
        //dbId = dbReportRepository.createVolunteeringPostReport(actor, reportedPostId, description);
        //this.memReport = new VolunteeringPostReport(memId, actor, description, reportedPostId);
        //this.dbReport = new VolunteeringPostReport(dbId, actor, description, reportedPostId);
    }

    @AfterEach
    void afterEach() {
        memoryReportRepository.clear();
        dbReportRepository.clear();
    }

    static Stream<ReportRepository> repoProvider() {
        return Stream.of(memoryReportRepository, dbReportRepository);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenValidFields_whenCreateReport_thenCreate(ReportRepository reportRepository) {
        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;

        List<Report> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(report1);

        List<Report> resBeforeAdd = reportRepository.getAllReports();
        assertEquals(expectedBeforeAdd, resBeforeAdd);

        //int reportId2 = reportRepository.createVolunteeringPostReport(actor, 1, "Very Bad");
        //Report report2 = new Report(reportId2, actor, "Very Bad", 1, ReportObject.VOLUNTEERING_POST);

        List<Report> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(report1);
        //expectedAfterAdd.add(report2);
        List<Report> resAfterAdd = reportRepository.getAllReports();
        assertEquals(expectedAfterAdd, resAfterAdd);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenInvalidFields_whenCreateReport_thenThrowException(ReportRepository reportRepository) {
        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;
        List<Report> expected = new ArrayList<>();
        expected.add(report1);

        List<Report> resBeforeAdd = reportRepository.getAllReports();
        assertEquals(expected, resBeforeAdd);


        //Exception exception = assertThrows(IllegalArgumentException.class, () -> reportRepository.createVolunteeringPostReport(actor, reportedPostId, ""));
        String expectedError = "Invalid report description: .";

        //assertEquals(expectedError, exception.getMessage());

        List<Report> resAfterAdd = reportRepository.getAllReports();
        assertEquals(expected, resAfterAdd);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenDoubleReport_whenCreateReport_thenThrowException(ReportRepository reportRepository) {
        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;

        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 10, 0);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(date);

            //int sameActorAnotherPostReportId = reportRepository.createVolunteeringPostReport(actor, 1, "description");
            //int samePostAnotherActorReportId = reportRepository.createVolunteeringPostReport("Dana", 0, "description");

            //Report sameActorAnotherPostReport = new Report(sameActorAnotherPostReportId, actor, "description", 1 + "", ReportObject.VOLUNTEERING_POST);
            //Report samePostAnotherActorReport = new Report(samePostAnotherActorReportId, "Dana", "description", 0 + "", ReportObject.VOLUNTEERING_POST);

            List<Report> expected = new ArrayList<>();
            expected.add(report1);
            //expected.add(sameActorAnotherPostReport);
            //expected.add(samePostAnotherActorReport);
            List<Report> resBeforeAdd = reportRepository.getAllReports();
            assertEquals(expected, resBeforeAdd);

            // Same date same actor same post
            //Exception exception = assertThrows(IllegalArgumentException.class, () -> reportRepository.createVolunteeringPostReport(actor, 0, "description"));
            String expectedError = ReportErrors.makeReportContentAlreadyExistsError();
            //assertEquals(expectedError, exception.getMessage());

            List<Report> resAfterAdd = reportRepository.getAllReports();
            assertEquals(expected, resAfterAdd);
        }
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenRemoveReport_thenRemove(ReportRepository reportRepository) {
        /*setReportByRepo(reportRepository);

        assertDoesNotThrow(() -> reportRepository.removeReport(id));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reportRepository.getReport(id);
        });
        assertEquals(ReportErrors.makeReportDoesNotExistError(id), exception.getMessage());*/
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenRemoveReport_thenThrowException(ReportRepository reportRepository) {
        /*setReportByRepo(reportRepository);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reportRepository.removeReport(id + 1);
        });
        assertEquals(ReportErrors.makeReportDoesNotExistError(id + 1), exception.getMessage());*/
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void removePostReports(ReportRepository reportRepository) {
        //int reportId2 = reportRepository.createVolunteeringPostReport("Another actor", 0, "Description");
        //int reportId3 = reportRepository.createVolunteeringPostReport(actor, 1, "Description");

        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;
        //Report report2 = new Report(reportId2, "Another actor", "Description", 0 + "", ReportObject.VOLUNTEERING_POST);
        //Report report3 = new Report(reportId3, actor, "Description", 1 + "", ReportObject.VOLUNTEERING_POST);

        //assertEquals(List.of(report1, report2, report3), reportRepository.getAllReports());
        //reportRepository.removePostReports(8);
        //assertEquals(List.of(report1, report2, report3), reportRepository.getAllReports());
        //reportRepository.removePostReports(0);
        //assertEquals(List.of(report3), reportRepository.getAllReports());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenValidFields_whenEditReport_thenEdit(ReportRepository reportRepository) {
        setReportByRepo(reportRepository);

        //assertDoesNotThrow(() -> reportRepository.editReport(id, "Very very offensive"));

        //Report expected = new VolunteeringPostReport(id, actor, "Very very offensive", reportedPostId);
        //assertEquals(expected, reportRepository.getReport(id));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenInvalidFields_whenEditReport_thenThrowException(ReportRepository reportRepository) {
        /*setReportByRepo(reportRepository);
        Report report = reportRepository == memoryReportRepository ? memReport : dbReport;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reportRepository.editReport(id, "");
        });

        assertEquals("Invalid report description: .", exception.getMessage());
        assertEquals(report, reportRepository.getReport(id));*/
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenEditReport_thenThrowException(ReportRepository reportRepository) {
        /*setReportByRepo(reportRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reportRepository.editReport(id + 1, "Description");
        });

        assertEquals(ReportErrors.makeReportDoesNotExistError(id + 1), exception.getMessage());*/
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenGetReport_thenReturnReport(ReportRepository reportRepository) {
        setReportByRepo(reportRepository);
        Report report = reportRepository == memoryReportRepository ? memReport : dbReport;
        //assertEquals(report, reportRepository.getReport(id));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenGetReport_thenThrowException(ReportRepository reportRepository) {
        setReportByRepo(reportRepository);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            //reportRepository.getReport(id + 1);
        });

        //assertEquals(ReportErrors.makeReportDoesNotExistError(id + 1), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void getAllReports(ReportRepository reportRepository) {
        Report expectedReport = reportRepository == memoryReportRepository ? memReport : dbReport;
        List<Report> expected = new ArrayList<>();
        expected.add(expectedReport);
        List<Report> res = reportRepository.getAllReports();
        assertEquals(expected, res);
    }

    private void setReportByRepo(ReportRepository repo) {
        this.id = repo == memoryReportRepository ? memId : dbId;
    }
}