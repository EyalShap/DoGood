package com.dogood.dogoodbackend.acceptance;

import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.domain.reports.ReportObject;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.*;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate; // Still needed for getVolunteeringPostReport if its signature requires LocalDate
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ReportAcceptanceTests {

    @MockitoBean
    ChatSocketSender chatSocketSender;
    @MockitoBean
    NotificationSocketSender notificationSocketSender;
    @MockitoBean
    FirebaseMessaging firebaseMessaging;
    @MockitoBean
    Gemini gemini;
    @MockitoBean
    EmailSender emailSender;
    @MockitoBean
    VerificationCacheService verificationCacheService;

    @Autowired
    UserService userService;
    @Autowired
    PostService postService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    VolunteeringService volunteeringService;
    @Autowired
    ReportService reportService;
    @Autowired
    FacadeManager facadeManager;

    private UsersFacade usersFacadeInstance;

    @Autowired
    MessageJPA messageJPA;
    @Autowired
    VolunteeringJPA volunteeringJPA;
    @Autowired
    OrganizationJPA organizationJPA;
    @Autowired
    NotificationJPA notificationJPA;
    @Autowired
    UserJPA userJPA;
    @Autowired
    VolunteerPostJPA volunteerPostJPA;
    @Autowired
    VolunteeringPostJPA volunteeringPostJPA;
    @Autowired
    ReportJPA reportJPA;
    @Autowired
    BannedJPA bannedJPA;

    private String aliceId, bobId, geriId, charlieId;
    private String aliceToken, bobToken, geriToken, charlieToken;
    private int orgGammaId, volunteeringDeltaId;
    private int volunteeringPostAlphaId, volunteerPostBetaId;

    @BeforeEach
    void setUp() {
        messageJPA.deleteAll();
        volunteeringJPA.deleteAll();
        organizationJPA.deleteAll();
        notificationJPA.deleteAll();
        userJPA.deleteAll();
        volunteerPostJPA.deleteAll();
        volunteeringPostJPA.deleteAll();
        reportJPA.deleteAll();
        bannedJPA.deleteAll();

        usersFacadeInstance = facadeManager.getUsersFacade();
        facadeManager.getAuthFacade().clearInvalidatedTokens();

        aliceId = "Alice";
        bobId = "Bobs";
        geriId = "Geri";
        charlieId = "Charlie";

        registerAndVerify(aliceId, "alice@example.com", "Alice Smith");
        registerAndVerify(bobId, "bob@example.com", "Bob Johnson");
        registerAndVerify(geriId, "geri@example.com", "Geri Halliwell");
        registerAndVerify(charlieId, "charlie@example.com", "Charlie Brown");

        Response<String> aliceLoginResponse = userService.login(aliceId, "password123");
        assertFalse(aliceLoginResponse.getError(), "SETUP FAIL: Alice login failed: " + aliceLoginResponse.getErrorString());
        aliceToken = aliceLoginResponse.getData();
        assertNotNull(aliceToken, "SETUP FAIL: Alice's token is null after login.");

        Response<String> bobLoginResponse = userService.login(bobId, "password123");
        assertFalse(bobLoginResponse.getError(), "SETUP FAIL: Bob login failed: " + bobLoginResponse.getErrorString());
        bobToken = bobLoginResponse.getData();
        assertNotNull(bobToken, "SETUP FAIL: Bob's token is null after login.");

        Response<String> geriLoginResponse = userService.login(geriId, "password123");
        assertFalse(geriLoginResponse.getError(), "SETUP FAIL: Geri login failed: " + geriLoginResponse.getErrorString());
        geriToken = geriLoginResponse.getData();
        assertNotNull(geriToken, "SETUP FAIL: Geri's token is null after login.");

        Response<String> charlieLoginResponse = userService.login(charlieId, "password123");
        assertFalse(charlieLoginResponse.getError(), "SETUP FAIL: Charlie login failed: " + charlieLoginResponse.getErrorString());
        charlieToken = charlieLoginResponse.getData();
        assertNotNull(charlieToken, "SETUP FAIL: Charlie's token is null after login.");

        userJPA.deleteById(aliceId);
        usersFacadeInstance.registerAdmin(aliceId, "password123", "Alice Smith (Admin)", "alice@example.com", "0501112233", new Date());
        aliceLoginResponse = userService.login(aliceId, "password123");
        assertFalse(aliceLoginResponse.getError(), "SETUP FAIL: Alice (admin) re-login failed: " + aliceLoginResponse.getErrorString());
        aliceToken = aliceLoginResponse.getData();
        assertNotNull(aliceToken, "SETUP FAIL: Alice's admin token is null after re-login.");

        Response<Integer> orgResponse = organizationService.createOrganization(aliceToken, "Organization Gamma", "Gamma Description", "0521231234", "gamma@org.com", aliceId);
        assertFalse(orgResponse.getError(), "SETUP FAIL: Failed to create Organization Gamma: " + orgResponse.getErrorString());
        assertNotNull(orgResponse.getData(), "SETUP FAIL: Organization Gamma ID is null after creation.");
        orgGammaId = orgResponse.getData();

        Response<Integer> volunteeringResponse = organizationService.createVolunteering(aliceToken, orgGammaId, "Volunteering Delta", "Delta Description", aliceId);
        assertFalse(volunteeringResponse.getError());
        assertNotNull(volunteeringResponse.getData());
        volunteeringDeltaId = volunteeringResponse.getData();

        Response<Integer> volunteeringPostResponse = postService.createVolunteeringPost(aliceToken, "Volunteering Post Alpha", "Alpha Description", aliceId, volunteeringDeltaId);
        assertFalse(volunteeringPostResponse.getError());
        assertNotNull(volunteeringPostResponse.getData());
        volunteeringPostAlphaId = volunteeringPostResponse.getData();

        Response<Integer> volunteerPostResponse = postService.createVolunteerPost(bobToken, bobId, "Volunteer Post Beta", "Beta Description");
        assertFalse(volunteerPostResponse.getError(), "SETUP FAIL: Failed to create Volunteer Post Beta: " + volunteerPostResponse.getErrorString());
        assertNotNull(volunteerPostResponse.getData(), "SETUP FAIL: Volunteer Post Beta ID is null after creation.");
        volunteerPostBetaId = volunteerPostResponse.getData();
    }

    private void registerAndVerify(String username, String email, String name) {
        usersFacadeInstance.register(username, "password123", name, email, "0500000000", new Date());
    }

    // Requirement 1.6: Reporting
    @Test
    void whenReportVolunteeringPost_givenValidData_thenReportIsSuccessfulAndPersisted() {
        String reportDescription = "This post seems misleading.";
        Response<ReportDTO> createReportResponse = reportService.createVolunteeringPostReport(bobToken, bobId, volunteeringPostAlphaId, reportDescription);

        assertFalse(createReportResponse.getError(), "Report submission should be successful.");
        ReportDTO createdReportDto = createReportResponse.getData();
        assertNotNull(createdReportDto, "Created report DTO should not be null.");
        assertEquals(bobId, createdReportDto.getReportingUser());
        assertEquals(String.valueOf(volunteeringPostAlphaId), createdReportDto.getReportedId());
        assertEquals(reportDescription, createdReportDto.getDescription());
        assertEquals(ReportObject.VOLUNTEERING_POST, createdReportDto.getReportObject());
        assertNotNull(createdReportDto.getDate(), "Report date should not be null."); // Check date is present

        // Verify persistence
        Response<ReportDTO> fetchedReportResponse = reportService.getVolunteeringPostReport(bobToken, bobId, createdReportDto.getDate(), volunteeringPostAlphaId, bobId);
        assertFalse(fetchedReportResponse.getError(), "Fetching the created report should be successful.");
        ReportDTO fetchedReportDto = fetchedReportResponse.getData();
        assertNotNull(fetchedReportDto, "Fetched report DTO should not be null.");
        assertEquals(createdReportDto.getReportingUser(), fetchedReportDto.getReportingUser());
        assertEquals(createdReportDto.getReportedId(), fetchedReportDto.getReportedId());
        assertEquals(createdReportDto.getDescription(), fetchedReportDto.getDescription());
        assertEquals(createdReportDto.getReportObject(), fetchedReportDto.getReportObject());
        assertEquals(createdReportDto.getDate(), fetchedReportDto.getDate());
    }
    @Test
    void whenReportVolunteerPost_givenValidData_thenReportIsSuccessfulAndPersisted() {
        String reportDescription = "This post seems misleading.";
        Response<ReportDTO> createReportResponse = reportService.createVolunteerPostReport(bobToken, bobId, volunteerPostBetaId, reportDescription);

        assertFalse(createReportResponse.getError(), "Report submission should be successful.");
        ReportDTO createdReportDto = createReportResponse.getData();
        assertNotNull(createdReportDto, "Created report DTO should not be null.");
        assertEquals(bobId, createdReportDto.getReportingUser());
        assertEquals(String.valueOf(volunteerPostBetaId), createdReportDto.getReportedId());
        assertEquals(reportDescription, createdReportDto.getDescription());
        assertEquals(ReportObject.VOLUNTEER_POST, createdReportDto.getReportObject());
        assertNotNull(createdReportDto.getDate(), "Report date should not be null.");
        Response<ReportDTO> fetchedReportResponse = reportService.getVolunteerPostReport(bobToken, bobId, createdReportDto.getDate(), volunteerPostBetaId, bobId);
        assertFalse(fetchedReportResponse.getError(), "Fetching the created report should be successful.");
        ReportDTO fetchedReportDto = fetchedReportResponse.getData();
        assertNotNull(fetchedReportDto, "Fetched report DTO should not be null.");
        assertEquals(createdReportDto.getReportingUser(), fetchedReportDto.getReportingUser());
        assertEquals(createdReportDto.getReportedId(), fetchedReportDto.getReportedId());
        assertEquals(createdReportDto.getDescription(), fetchedReportDto.getDescription());
        assertEquals(createdReportDto.getReportObject(), fetchedReportDto.getReportObject());
        assertEquals(createdReportDto.getDate(), fetchedReportDto.getDate());
    }

    @Test
    void whenReportUser_givenValidData_thenReportIsSuccessfulAndPersisted() {
        String reportDescription = "Suspicious activity on this profile.";
        Response<ReportDTO> createReportResponse = reportService.createUserReport(geriToken, geriId, bobId, reportDescription);

        assertFalse(createReportResponse.getError());
        ReportDTO createdReportDto = createReportResponse.getData();
        assertNotNull(createdReportDto);
        assertEquals(geriId, createdReportDto.getReportingUser());
        assertEquals(bobId, createdReportDto.getReportedId());
        assertEquals(reportDescription, createdReportDto.getDescription());
        assertEquals(ReportObject.USER, createdReportDto.getReportObject());
        assertNotNull(createdReportDto.getDate());

        // Verify persistence
        Response<ReportDTO> fetchedReportResponse = reportService.getUserReport(geriToken, geriId, createdReportDto.getDate(), bobId, geriId);
        assertFalse(fetchedReportResponse.getError(), "Fetching the created user report should be successful.");
        ReportDTO fetchedReportDto = fetchedReportResponse.getData();
        assertNotNull(fetchedReportDto, "Fetched user report DTO should not be null.");
        assertEquals(createdReportDto, fetchedReportDto);
    }

    @Test
    void whenReportOrganization_givenValidData_thenReportIsSuccessfulAndPersisted() {
        String reportDescription = "This organization seems fake.";
        Response<ReportDTO> createReportResponse = reportService.createOrganizationReport(bobToken, bobId, orgGammaId, reportDescription);

        assertFalse(createReportResponse.getError());
        ReportDTO createdReportDto = createReportResponse.getData();
        assertNotNull(createdReportDto);
        assertEquals(bobId, createdReportDto.getReportingUser());
        assertEquals(String.valueOf(orgGammaId), createdReportDto.getReportedId());
        assertEquals(reportDescription, createdReportDto.getDescription());
        assertEquals(ReportObject.ORGANIZATION, createdReportDto.getReportObject());
        assertNotNull(createdReportDto.getDate());

        // Verify persistence
        Response<ReportDTO> fetchedReportResponse = reportService.getOrganizationReport(bobToken, bobId, createdReportDto.getDate(), orgGammaId, bobId);
        assertFalse(fetchedReportResponse.getError(), "Fetching the created organization report should be successful.");
        ReportDTO fetchedReportDto = fetchedReportResponse.getData();
        assertNotNull(fetchedReportDto, "Fetched organization report DTO should not be null.");
        assertEquals(createdReportDto, fetchedReportDto);
    }

    @Test
    void whenReportVolunteering_givenValidData_thenReportIsSuccessfulAndPersisted() {
        String reportDescription = "This volunteering is not as described.";
        Response<ReportDTO> createReportResponse = reportService.createVolunteeringReport(bobToken, bobId, volunteeringDeltaId, reportDescription);

        assertFalse(createReportResponse.getError());
        ReportDTO createdReportDto = createReportResponse.getData();
        assertNotNull(createdReportDto);
        assertEquals(bobId, createdReportDto.getReportingUser());
        assertEquals(String.valueOf(volunteeringDeltaId), createdReportDto.getReportedId());
        assertEquals(reportDescription, createdReportDto.getDescription());
        assertEquals(ReportObject.VOLUNTEERING, createdReportDto.getReportObject());
        assertNotNull(createdReportDto.getDate());

        // Verify persistence
        Response<ReportDTO> fetchedReportResponse = reportService.getVolunteeringReport(bobToken, bobId, createdReportDto.getDate(), volunteeringDeltaId, bobId);
        assertFalse(fetchedReportResponse.getError(), "Fetching the created volunteering report should be successful.");
        ReportDTO fetchedReportDto = fetchedReportResponse.getData();
        assertNotNull(fetchedReportDto, "Fetched volunteering report DTO should not be null.");
        assertEquals(createdReportDto, fetchedReportDto);
    }


    @Test
    void whenReportNonExistentItem_givenVolunteeringPost_thenErrorReturned() {
        int nonExistentPostId = 99999;
        Response<ReportDTO> reportResponse = reportService.createVolunteeringPostReport(bobToken, bobId, nonExistentPostId, "Reporting non-existent post.");
        assertTrue(reportResponse.getError());
    }
    @Test
    void whenReportNonExistentItem_givenVolunteerPost_thenErrorReturned() {
        int nonExistentPostId = 99999;
        Response<ReportDTO> reportResponse = reportService.createVolunteerPostReport(bobToken, bobId, nonExistentPostId, "Reporting non-existent post.");
        assertTrue(reportResponse.getError());
    }
    @Test
    void whenReportNonExistentItem_givenVolunteering_thenErrorReturned() {
        int nonExistentPostId = 99999;
        Response<ReportDTO> reportResponse = reportService.createVolunteeringReport(bobToken, bobId, nonExistentPostId, "Reporting non-existent post.");
        assertTrue(reportResponse.getError());
    }
    @Test
    void whenReportNonExistentItem_givenOrganization_thenErrorReturned() {
        int nonExistentPostId = 99999;
        Response<ReportDTO> reportResponse = reportService.createOrganizationReport(bobToken, bobId, nonExistentPostId, "Reporting non-existent post.");
        assertTrue(reportResponse.getError());
    }
    @Test
    void whenReportNonExistentItem_givenUser_thenErrorReturned() {
        String nonExistentUserName = "Yosef";
        Response<ReportDTO> reportResponse = reportService.createUserReport(bobToken, bobId, nonExistentUserName, "Reporting non-existent post.");
        assertTrue(reportResponse.getError());
    }

    @Test
    void whenReportItemTwiceSameDay_givenVolunteeringPost_thenSecondReportFails() {
        reportService.createVolunteeringPostReport(bobToken, bobId, volunteeringPostAlphaId, "First report today.");
        Response<ReportDTO> secondReportResponse = reportService.createVolunteeringPostReport(bobToken, bobId, volunteeringPostAlphaId, "Second report today.");
        assertTrue(secondReportResponse.getError());
    }
    @Test
    void whenReportItemTwiceSameDay_givenVolunteerPost_thenSecondReportFails() {
        reportService.createVolunteerPostReport(bobToken, bobId, volunteerPostBetaId, "First report today.");
        Response<ReportDTO> secondReportResponse = reportService.createVolunteerPostReport(bobToken, bobId, volunteerPostBetaId, "Second report today.");
        assertTrue(secondReportResponse.getError());
    }
    @Test
    void whenReportItemTwiceSameDay_givenOrganization_thenSecondReportFails() {
        reportService.createOrganizationReport(bobToken, bobId, orgGammaId, "First report today.");
        Response<ReportDTO> secondReportResponse = reportService.createOrganizationReport(bobToken, bobId, orgGammaId, "Second report today.");
        assertTrue(secondReportResponse.getError());
    }
    @Test
    void whenReportItemTwiceSameDay_givenUser_thenSecondReportFails() {
        reportService.createUserReport(bobToken, bobId,geriId , "First report today.");
        Response<ReportDTO> secondReportResponse = reportService.createUserReport(bobToken, bobId, geriId, "Second report today.");
        assertTrue(secondReportResponse.getError());
    }
    @Test
    void whenReportItemTwiceSameDay_givenVolunteering_thenSecondReportFails() {
        reportService.createVolunteeringReport(bobToken, bobId, volunteeringDeltaId, "First report today.");
        Response<ReportDTO> secondReportResponse = reportService.createVolunteeringReport(bobToken, bobId, volunteeringDeltaId, "Second report today.");
        assertTrue(secondReportResponse.getError());
    }

    @Test
    void whenReportItemWithEmptyDescription_givenVolunteeringPost_thenErrorReturned() {
        Response<ReportDTO> reportResponse = reportService.createVolunteeringPostReport(bobToken, bobId, volunteeringPostAlphaId, "");
        assertTrue(reportResponse.getError());
    }

    // Requirement 5.1: View Reports
    @Test
    void whenViewReports_givenUserIsAdminAndReportsExist_thenAllReportsAreDisplayed() {
        reportService.createUserReport(geriToken, geriId, bobId, "Bob's profile is suspicious.");

        Response<List<ReportDTO>> reportsResponse = reportService.getAllReportDTOs(aliceToken, aliceId);

        assertFalse(reportsResponse.getError());
        List<ReportDTO> reports = reportsResponse.getData();
        assertNotNull(reports);
        assertEquals(1, reports.size());
    }
    @Test
    void whenViewReports_givenNoReports_thenEmptyList() {
        Response<List<ReportDTO>> reportsResponse = reportService.getAllReportDTOs(aliceToken, aliceId);
        assertFalse(reportsResponse.getError());
        List<ReportDTO> reports = reportsResponse.getData();
        assertNotNull(reports);
        assertEquals(0, reports.size());
    }


    @Test
    void whenViewReports_givenUserIsNotAdmin_thenAccessDenied() {
        reportService.createUserReport(geriToken, geriId, charlieId, "Charlie's reportable offense.");
        Response<List<ReportDTO>> reportsResponse = reportService.getAllReportDTOs(bobToken, bobId);
        assertTrue(reportsResponse.getError());
    }

    // Requirement 5.2: Suspend User
    @Test
    void whenSuspendUser_givenUserIsAdminAndTargetUserExists_thenUserIsBannedAndCannotLogin() {
        User bobBeforeBan = usersFacadeInstance.getUser(bobId);
        assertFalse(facadeManager.getReportsFacade().isBannedEmail(bobBeforeBan.getEmails().get(0)));

        Response<Boolean> banResponse = userService.banUser(aliceToken, aliceId, bobId);

        assertFalse(banResponse.getError());
        assertTrue(banResponse.getData());

        User bobAfterBan = usersFacadeInstance.getUser(bobId);
        assertTrue(facadeManager.getReportsFacade().isBannedEmail(bobAfterBan.getEmails().get(0)));

        Response<String> loginAttempt = userService.login(bobId, "password123");
        assertTrue(loginAttempt.getError());
    }

    @Test
    void whenSuspendUser_givenUserIsNotAdmin_thenUserNotBanned() {
        User bobBeforeAttempt = usersFacadeInstance.getUser(bobId);
        assertFalse(facadeManager.getReportsFacade().isBannedEmail(bobBeforeAttempt.getEmails().get(0)));

        Response<Boolean> banResponse = userService.banUser(geriToken, geriId, bobId);

        assertTrue(banResponse.getError());

        User bobAfterAttempt = usersFacadeInstance.getUser(bobId);
        assertFalse(facadeManager.getReportsFacade().isBannedEmail(bobAfterAttempt.getEmails().get(0)));
    }

    @Test
    void whenSuspendNonExistentUser_givenUserIsAdmin_thenErrorReturned() {
        String nonExistentUserId = "NonExistentUser123";
        Response<Boolean> banResponse = userService.banUser(aliceToken, aliceId, nonExistentUserId);
        assertTrue(banResponse.getError());
    }

    @Test
    void whenLogin_givenUserIsBanned_thenLoginFails() {
        userService.banUser(aliceToken, aliceId, bobId);
        userService.logout(bobToken);

        Response<String> loginAttempt = userService.login(bobId, "password123");
        assertTrue(loginAttempt.getError());
    }
}