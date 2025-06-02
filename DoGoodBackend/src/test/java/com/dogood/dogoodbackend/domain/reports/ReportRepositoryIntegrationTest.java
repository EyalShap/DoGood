package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.jparepos.ReportJPA;
import com.dogood.dogoodbackend.utils.ReportErrors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
    private final String reportedPostId = "0";
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

        memReport = memoryReportRepository.createReport(actor, reportedPostId, description, ReportObject.VOLUNTEERING_POST);
        dbReport = dbReportRepository.createReport(actor, reportedPostId, description, ReportObject.VOLUNTEERING_POST);
    }

    @AfterEach
    void afterEach() {
        memoryReportRepository.clear();
        dbReportRepository.clear();
    }

    static Stream<ReportRepository> repoProvider() {
        return Stream.of(memoryReportRepository, dbReportRepository);
    }

    private void assertEqualsReportList(List<Report> expected, List<Report> received) {
        // assert equivalence between report lists
        assertTrue(expected.stream().allMatch(r1->received.stream().anyMatch(r2->r2.equals(r1)))
                && received.stream().allMatch(r1->expected.stream().anyMatch(r2->r2.equals(r1))));
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenValidFields_whenCreateReport_thenCreate(ReportRepository reportRepository) {
        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;

        List<Report> expectedBeforeAdd = new ArrayList<>();
        expectedBeforeAdd.add(report1);

        List<Report> resBeforeAdd = reportRepository.getAllReports();
        assertEqualsReportList(expectedBeforeAdd, resBeforeAdd);

        Report report2 = reportRepository.createReport(actor,"1","Very Bad",ReportObject.VOLUNTEERING_POST);

        List<Report> expectedAfterAdd = new ArrayList<>();
        expectedAfterAdd.add(report1);
        expectedAfterAdd.add(report2);
        List<Report> resAfterAdd = reportRepository.getAllReports();
        assertEqualsReportList(expectedAfterAdd, resAfterAdd);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenInvalidFields_whenCreateReport_thenThrowException(ReportRepository reportRepository) {
        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;
        List<Report> expected = new ArrayList<>();
        expected.add(report1);

        List<Report> resBeforeAdd = reportRepository.getAllReports();
        assertEqualsReportList(expected, resBeforeAdd);


        Exception exception = assertThrows(IllegalArgumentException.class, () -> reportRepository.createReport(actor, reportedPostId, "", ReportObject.VOLUNTEERING_POST));
        String expectedError = "Invalid report description: .";

        assertEquals(expectedError, exception.getMessage());

        List<Report> resAfterAdd = reportRepository.getAllReports();
        assertEqualsReportList(expected, resAfterAdd);
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenDoubleReport_whenCreateReport_thenThrowException(ReportRepository reportRepository) {
        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;

        LocalDateTime date = LocalDateTime.of(2025, 1, 1, 10, 0);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(date);

            Report sameActorAnotherPostReport = reportRepository.createReport(actor, "1", "description", ReportObject.VOLUNTEERING_POST);
            Report samePostAnotherActorReport = reportRepository.createReport("Dana", "0", "description", ReportObject.VOLUNTEERING_POST);

            //Report sameActorAnotherPostReport = new Report(sameActorAnotherPostReportId, actor, "description", 1 + "", ReportObject.VOLUNTEERING_POST);
            //Report samePostAnotherActorReport = new Report(samePostAnotherActorReportId, "Dana", "description", 0 + "", ReportObject.VOLUNTEERING_POST);

            List<Report> expected = new ArrayList<>();
            expected.add(report1);
            expected.add(sameActorAnotherPostReport);
            expected.add(samePostAnotherActorReport);
            List<Report> resBeforeAdd = reportRepository.getAllReports();

            // assert equivalence between report lists
            assertEqualsReportList(expected,resBeforeAdd);

            // Same date same actor same post
            Exception exception = assertThrows(IllegalArgumentException.class, () -> reportRepository.createReport(actor, reportedPostId, description, ReportObject.VOLUNTEERING_POST));
            String expectedError = ReportErrors.makeReportAlreadyExistsError(new ReportKey(actor, report1.getDate(), reportedPostId, ReportObject.VOLUNTEERING_POST));//makeReportContentAlreadyExistsError();
            assertEquals(expectedError, exception.getMessage());

            List<Report> resAfterAdd = reportRepository.getAllReports();
            // assert equivalence between report lists
            assertEqualsReportList(expected,resAfterAdd);
        }
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenExistingId_whenRemoveReport_thenRemove(ReportRepository reportRepository) {
        setReportByRepo(reportRepository);
        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;

        assertDoesNotThrow(() -> reportRepository.removeReport(report1.getReportingUser(),report1.getDate(),report1.getReportedId(),report1.getReportObject()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reportRepository.getReport(report1.getReportingUser(),report1.getDate(),report1.getReportedId(),report1.getReportObject());
        });
        assertEquals(ReportErrors.makeReportDoesNotExistError(new ReportKey(report1.getReportingUser(),report1.getDate(),report1.getReportedId(),report1.getReportObject())), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void givenNonExistingId_whenRemoveReport_thenThrowException(ReportRepository reportRepository) {
        setReportByRepo(reportRepository);
        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;
        String nonExistentReportedId = report1.getReportedId()+"1111";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            reportRepository.removeReport(id + 1);
            reportRepository.removeReport(report1.getReportingUser(),report1.getDate(),nonExistentReportedId,report1.getReportObject());
        });
//        assertEquals(ReportErrors.makeReportDoesNotExistError(id + 1), exception.getMessage());
        assertEquals(ReportErrors.makeReportDoesNotExistError(new ReportKey(report1.getReportingUser(),report1.getDate(),nonExistentReportedId,report1.getReportObject())), exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void removePostReports(ReportRepository reportRepository) {
        Report report2 = reportRepository.createReport("Another actor", "0", "Description",ReportObject.VOLUNTEERING);
        Report report3 = reportRepository.createReport(actor, "1", "Description", ReportObject.VOLUNTEERING_POST);

        Report report1 = reportRepository == memoryReportRepository ? memReport : dbReport;

        // assert equivalence between report lists
        //assertEquals(List.of(report1, report2, report3), reportRepository.getAllReports());
        assertEqualsReportList(List.of(report1, report2, report3), reportRepository.getAllReports());

        reportRepository.removeReport(report2.getReportingUser(),report2.getDate(),report2.getReportedId(),report2.getReportObject()); // removePostReports(8);
        // assert equivalence between report lists
//        assertEquals(List.of(report1, report3), reportRepository.getAllReports());
        assertEqualsReportList(List.of(report1, report3), reportRepository.getAllReports());

        reportRepository.removeReport(report1.getReportingUser(),report1.getDate(),report1.getReportedId(),report1.getReportObject()); //removePostReports(0);
        // assert equivalence between report lists
//        assertEquals(List.of(report3), reportRepository.getAllReports());
        assertEqualsReportList(List.of(report3), reportRepository.getAllReports());

    }

    // NOTE: we don't need to check Edit and Get
//    @ParameterizedTest
//    @MethodSource("repoProvider")
//    void givenValidFields_whenEditReport_thenEdit(ReportRepository reportRepository) {
//        setReportByRepo(reportRepository);
//
//        //assertDoesNotThrow(() -> reportRepository.editReport(id, "Very very offensive"));
//
//        //Report expected = new VolunteeringPostReport(id, actor, "Very very offensive", reportedPostId);
//        //assertEquals(expected, reportRepository.getReport(id));
//    }
//
//    @ParameterizedTest
//    @MethodSource("repoProvider")
//    void givenInvalidFields_whenEditReport_thenThrowException(ReportRepository reportRepository) {
//        /*setReportByRepo(reportRepository);
//        Report report = reportRepository == memoryReportRepository ? memReport : dbReport;
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            reportRepository.editReport(id, "");
//        });
//
//        assertEquals("Invalid report description: .", exception.getMessage());
//        assertEquals(report, reportRepository.getReport(id));*/
//    }
//
//    @ParameterizedTest
//    @MethodSource("repoProvider")
//    void givenNonExistingId_whenEditReport_thenThrowException(ReportRepository reportRepository) {
//        /*setReportByRepo(reportRepository);
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            reportRepository.editReport(id + 1, "Description");
//        });
//
//        assertEquals(ReportErrors.makeReportDoesNotExistError(id + 1), exception.getMessage());*/
//    }
//
//    @ParameterizedTest
//    @MethodSource("repoProvider")
//    void givenExistingId_whenGetReport_thenReturnReport(ReportRepository reportRepository) {
//        setReportByRepo(reportRepository);
//        Report report = reportRepository == memoryReportRepository ? memReport : dbReport;
//        //assertEquals(report, reportRepository.getReport(id));
//    }
//
//    @ParameterizedTest
//    @MethodSource("repoProvider")
//    void givenNonExistingId_whenGetReport_thenThrowException(ReportRepository reportRepository) {
//        setReportByRepo(reportRepository);
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            //reportRepository.getReport(id + 1);
//        });
//
//        //assertEquals(ReportErrors.makeReportDoesNotExistError(id + 1), exception.getMessage());
//    }

    @ParameterizedTest
    @MethodSource("repoProvider")
    void getAllReports(ReportRepository reportRepository) {
        Report expectedReport = reportRepository == memoryReportRepository ? memReport : dbReport;
        List<Report> expected = new ArrayList<>();
        expected.add(expectedReport);
        List<Report> res = reportRepository.getAllReports();
        // Note: the normal equals works in DB just because it has only one report, otherwise do as done previously
//        assertEquals(expected, res);
        assertEqualsReportList(expected, res);
    }

    private void setReportByRepo(ReportRepository repo) {
        this.id = repo == memoryReportRepository ? memId : dbId;
    }
}