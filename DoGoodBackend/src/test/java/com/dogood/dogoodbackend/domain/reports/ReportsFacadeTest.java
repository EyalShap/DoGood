package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.organizations.*;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.PushNotificationSender;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.FacadeManager;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class ReportsFacadeTest {
    @Autowired
    private VolunteeringJPA volunteeringJPA;
    @Autowired
    private HourRequestJPA hourRequestJPA;
    @Autowired
    private AppointmentJPA appointmentJPA;
    @Autowired
    private OrganizationJPA organizationJPA;
    @Autowired
    private UserJPA userJPA;
    @Autowired
    private NotificationJPA notificationJPA;
    @Autowired
    private ReportJPA reportJPA;

    @MockitoBean
    private Gemini gemini;
    @MockitoBean
    private PushNotificationSender pushNotificationSender;
    @MockitoBean
    private NotificationSocketSender notificationSocketSender;

    @Autowired
    private FacadeManager facadeManager;

    private UsersFacade usersFacade;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private ReportsFacade reportsFacade;
    private PostsFacade postsFacade;

    private int organizationId, volunteeringId, volunteeringPostId, volunteerPostId;
    private int volunteeringPostId2, volunteeringPostId3, volunteerPostId2, volunteerPostId3, organizationId2, organizationId3, volunteeringId2, volunteeringId3;
    private ReportDTO reportDTO;
    private final String username1 = "TheDoctor";
    private final String username2 = "User2";
    private final String username3 = "User3";
    private final String username4 = "User4";
    private final String adminUsername = "Admin";

    @BeforeEach
    public void setUp() {
        volunteeringJPA.deleteAll();
        hourRequestJPA.deleteAll();
        appointmentJPA.deleteAll();
        organizationJPA.deleteAll();
        userJPA.deleteAll();
        notificationJPA.deleteAll();
        reportJPA.deleteAll();

        facadeManager.getNotificationSystem().setPushNotificationSender(pushNotificationSender);
        facadeManager.getNotificationSystem().setSender(notificationSocketSender);

        usersFacade = facadeManager.getUsersFacade();
        volunteeringFacade = facadeManager.getVolunteeringFacade();
        organizationsFacade = facadeManager.getOrganizationsFacade();
        reportsFacade = facadeManager.getReportsFacade();
        postsFacade = facadeManager.getPostsFacade();

        usersFacade.register(username1, "123456", "Name", "email@email.com","052-0520520",new Date());
        usersFacade.register(username2, "123456", "Name", "email@email.com","052-0520520",new Date());
        usersFacade.register(username3, "123456", "Name", "email@email.com","052-0520520",new Date());
        usersFacade.register(username4, "123456", "Name", "email@email.com","052-0520520",new Date());
        usersFacade.registerAdmin(adminUsername,"123456", "Name", "email@email.com","052-0520520",new Date());

        this.organizationId = this.organizationsFacade.createOrganization("Organization", "Description", "0547960995", "org@gmail.com", username1);
        this.organizationId2 = this.organizationsFacade.createOrganization("Organization2", "Description", "0547960995", "org@gmail.com", username1);
        this.organizationId3 = this.organizationsFacade.createOrganization("Organization3", "Description", "0547960995", "org@gmail.com", username1);
        this.volunteeringId = this.volunteeringFacade.createVolunteering(username1, organizationId, "Volunteering", "Description");
        this.volunteeringId2 = this.volunteeringFacade.createVolunteering(username1, organizationId2, "Volunteering", "Description");
        this.volunteeringId3 = this.volunteeringFacade.createVolunteering(username1, organizationId3, "Volunteering", "Description");
        this.volunteeringPostId = this.postsFacade.createVolunteeringPost("Title", "Description", username1, volunteeringId);
        this.volunteeringPostId2 = this.postsFacade.createVolunteeringPost("Title", "Description", username1, volunteeringId2);
        this.volunteeringPostId3 = this.postsFacade.createVolunteeringPost("Title", "Description", username1, volunteeringId3);
        this.volunteerPostId = this.postsFacade.createVolunteerPost("Title", "Description", username1);
        this.volunteerPostId2 = this.postsFacade.createVolunteerPost("Title", "Description", username2);
        this.volunteerPostId3 = this.postsFacade.createVolunteerPost("Title", "Description", username3);
//        this.reportDTO = this.reportsFacade.createVolunteeringPostReport(username1, postId, "Description");
    }

    @Test
    void givenValidFields_whenCreateUserReport_thenReportCreated() {
        String description = "givenValidFields_whenCreateUserReport_thenPostCreated";
        ReportDTO dto_create = reportsFacade.createUserReport(username1,username2,description);
        Assertions.assertDoesNotThrow(() -> reportsFacade.getUserReport(username1,dto_create.getDate(),username2,username1));
        ReportDTO dto = reportsFacade.getUserReport(username1,dto_create.getDate(),username2,username1); // check dto get
        Assertions.assertEquals(dto.getReportingUser(),username1);
        Assertions.assertEquals(dto.getReportedId(),username2);
        Assertions.assertEquals(dto.getDescription(),description);
    }

    @Test
    void givenNonExistentUser_whenCreateUserReport_thenReportNotCreated() {
        String description = "givenNonExistentUser_whenCreateUserReport_thenReportNotCreated";
        String nonExistentUser = "non-existent";
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createUserReport(username1,nonExistentUser,description));
    }

    @Test
    void givenInvalidDescription_whenCreateUserReport_thenReportNotCreated() {
        String description = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createUserReport(username1,username2,description));
    }

    @Test
    void givenValidFields_whenCreateVolunteeringPostReport_thenReportCreated() {
        String description = "givenValidFields_whenCreateVolunteeringPostReport_thenReportCreated";
        ReportDTO dto_create = reportsFacade.createVolunteeringPostReport(username1,volunteeringPostId,description);
        Assertions.assertDoesNotThrow(() -> reportsFacade.getVolunteeringPostReport(username1,dto_create.getDate(),volunteeringPostId,username1));
        ReportDTO dto = reportsFacade.getVolunteeringPostReport(username1,dto_create.getDate(), volunteeringPostId,username1); // check dto get
        Assertions.assertEquals(dto.getReportingUser(),username1);
        Assertions.assertEquals(dto.getReportedId(), volunteeringPostId + "");
        Assertions.assertEquals(dto.getDescription(),description);
    }

    @Test
    void givenNonExistentUser_whenCreateVolunteeringPostReport_thenReportNotCreated() {
        String description = "givenNonExistentUser_whenCreateVolunteeringPostReport_thenReportNotCreated";
        int invalidPostId = -2;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createVolunteeringPostReport(username1,invalidPostId,description));
    }

    @Test
    void givenInvalidDescription_whenCreateVolunteeringPostReport_thenReportNotCreated() {
        String description = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createVolunteeringPostReport(username1,volunteeringPostId,description));
    }

    @Test
    void givenValidFields_whenCreateVolunteerPostReport_thenReportCreated() {
        String description = "givenValidFields_whenCreateVolunteerPostReport_thenReportCreated";
        ReportDTO dto_create = reportsFacade.createVolunteerPostReport(username1,volunteerPostId,description);
        Assertions.assertDoesNotThrow(() -> reportsFacade.getVolunteerPostReport(username1,dto_create.getDate(),volunteerPostId,username1));
        ReportDTO dto = reportsFacade.getVolunteerPostReport(username1,dto_create.getDate(),volunteerPostId,username1); // check dto get
        Assertions.assertEquals(dto.getReportingUser(),username1);
        Assertions.assertEquals(dto.getReportedId(),volunteerPostId + "");
        Assertions.assertEquals(dto.getDescription(),description);
    }

    @Test
    void givenNonExistentUser_whenCreateVolunteerPostReport_thenReportNotCreated() {
        String description = "givenNonExistentUser_whenCreateVolunteerPostReport_thenReportNotCreated";
        int invalidPostId = -2;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createVolunteerPostReport(username1,invalidPostId,description));
    }

    @Test
    void givenInvalidDescription_whenCreateVolunteerPostReport_thenReportNotCreated() {
        String description = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createVolunteerPostReport(username1,volunteerPostId,description));
    }

    @Test
    void givenValidFields_whenCreateOrganizationReport_thenReportCreated() {
        String description = "givenValidFields_whenCreateOrganizationReport_thenReportCreated";
        ReportDTO dto_create = reportsFacade.createOrganizationReport(username1,organizationId,description);
        Assertions.assertDoesNotThrow(() -> reportsFacade.getOrganizationReport(username1,dto_create.getDate(),organizationId,username1));
        ReportDTO dto = reportsFacade.getOrganizationReport(username1,dto_create.getDate(),organizationId,username1); // check dto get
        Assertions.assertEquals(dto.getReportingUser(),username1);
        Assertions.assertEquals(dto.getReportedId(),organizationId + "");
        Assertions.assertEquals(dto.getDescription(),description);
    }

    @Test
    void givenNonExistentUser_whenCreateOrganizationReport_thenReportNotCreated() {
        String description = "givenNonExistentUser_whenCreateOrganizationReport_thenReportNotCreated";
        int invalidOrganizationId = -2;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createOrganizationReport(username1,invalidOrganizationId,description));
    }

    @Test
    void givenInvalidDescription_whenCreateOrganizationReport_thenReportNotCreated() {
        String description = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createOrganizationReport(username1,organizationId,description));
    }

    @Test
    void givenValidFields_whenCreateVolunteeringReport_thenReportCreated() {
        String description = "givenValidFields_whenCreateVolunteeringReport_thenReportCreated";
        ReportDTO dto_create = reportsFacade.createVolunteeringReport(username1,volunteeringId,description);
        Assertions.assertDoesNotThrow(() -> reportsFacade.getVolunteeringReport(username1,dto_create.getDate(),volunteeringId,username1));
        ReportDTO dto = reportsFacade.getVolunteeringReport(username1,dto_create.getDate(),volunteeringId,username1); // check dto get
        Assertions.assertEquals(dto.getReportingUser(),username1);
        Assertions.assertEquals(dto.getReportedId(),volunteeringId + "");
        Assertions.assertEquals(dto.getDescription(),description);
    }

    @Test
    void givenNonExistentUser_whenCreateVolunteeringReport_thenReportNotCreated() {
        String description = "givenNonExistentUser_whenCreateVolunteeringReport_thenReportNotCreated";
        int invalidVolunteeringId = -2;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createVolunteeringReport(username1,invalidVolunteeringId,description));
    }

    @Test
    void givenInvalidDescription_whenCreateVolunteeringReport_thenReportNotCreated() {
        String description = null;
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.createVolunteeringReport(username1,volunteeringId,description));
    }

    @Test
    void givenValidFields_whenRemoveUserReport_thenReportRemoved() {
        // Setup - create a user report
        String description = "givenValidFields_whenRemoveUserReport_thenReportRemoved";
        ReportDTO dto_create = reportsFacade.createUserReport(username1,username2,description);

        Assertions.assertDoesNotThrow(() -> reportsFacade.removeUserReport(username1,dto_create.getDate(),username2,username1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getUserReport(username1,dto_create.getDate(),username2,username1)); // ensure get won't return it
    }

    @Test
    void givenValidFields_whenRemoveVolunteeringPostReport_thenReportRemoved() {
        // Setup - create a voluntering post report
        String description = "givenValidFields_whenRemoveVolunteeringPostReport_thenReportRemoved";
        ReportDTO dto_create = reportsFacade.createVolunteeringPostReport(username1,volunteeringPostId,description);

        Assertions.assertDoesNotThrow(() -> reportsFacade.removeVolunteeringPostReport(username1,dto_create.getDate(),volunteeringPostId,username1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getVolunteeringPostReport(username1,dto_create.getDate(),volunteeringPostId,username1)); // ensure get won't return it
    }

    @Test
    void givenValidFields_whenRemoveVolunteerPostReport_thenReportRemoved() {
        // Setup - create a volunteer post report
        String description = "givenValidFields_whenRemoveVolunteerPostReport_thenReportRemoved";
        ReportDTO dto_create = reportsFacade.createVolunteerPostReport(username1,volunteerPostId,description);

        Assertions.assertDoesNotThrow(() -> reportsFacade.removeVolunteerPostReport(username1,dto_create.getDate(),volunteerPostId,username1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getVolunteerPostReport(username1,dto_create.getDate(),volunteerPostId,username1)); // ensure get won't return it
    }

    @Test
    void givenValidFields_whenRemoveOrganizationReport_thenReportRemoved() {
        // Setup - create an organization report
        String description = "givenValidFields_whenRemoveOrganizationReport_thenReportRemoved";
        ReportDTO dto_create = reportsFacade.createOrganizationReport(username1,organizationId,description);

        Assertions.assertDoesNotThrow(() -> reportsFacade.removeOrganizationReport(username1,dto_create.getDate(),organizationId,username1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getOrganizationReport(username1,dto_create.getDate(),organizationId,username1)); // ensure get won't return it
    }

    @Test
    void givenValidFields_whenRemoveVolunteeringReport_thenReportRemoved() {
        // Setup - create a volunteering report
        String description = "givenValidFields_whenRemoveVolunteeringReport_thenReportRemoved";
        ReportDTO dto_create = reportsFacade.createVolunteeringReport(username1,volunteeringId,description);

        Assertions.assertDoesNotThrow(() -> reportsFacade.removeVolunteeringReport(username1,dto_create.getDate(),volunteeringId,username1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.removeVolunteeringReport(username1,dto_create.getDate(),volunteeringId,username1)); // ensure get won't return it
    }

    // getReport - each one checked in createReport functions

    @Test
    void givenAdmin_whenGetAllReportDTOs_thenReturnsAllReports() {
        // Setup - create some reports
        String description = "givenAdmin_whenGetAllReportDTOs_thenReturnsAllReports";
        reportsFacade.createUserReport(username1,username2,description);
        reportsFacade.createVolunteeringPostReport(username1,volunteeringPostId,description);
        reportsFacade.createVolunteerPostReport(username1,volunteerPostId,description);
        reportsFacade.createOrganizationReport(username1,organizationId,description);
        reportsFacade.createVolunteeringReport(username1,volunteeringId,description);


        List<ReportDTO> reports = reportsFacade.getAllReportDTOs(adminUsername); // ensure all reports are returned
        Assertions.assertEquals(5, reports.size());
        Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(username2)
                && r.getReportingUser().equals(username1)
                && r.getDescription().equals(description)));
        Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(volunteeringPostId+"")
                && r.getReportingUser().equals(username1)
                && r.getDescription().equals(description)));
        Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(volunteerPostId+"")
                && r.getReportingUser().equals(username1)
                && r.getDescription().equals(description)));
        Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(organizationId+"")
                && r.getReportingUser().equals(username1)
                && r.getDescription().equals(description)));
        Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(volunteeringId+"")
                && r.getReportingUser().equals(username1)
                && r.getDescription().equals(description)));
    }

    @Test
    void givenNotAdmin_whenGetAllReportDTOs_thenError() {
        // Setup - create some reports
        String description = "givenNotAdmin_whenGetAllReportDTOs_thenError";
        reportsFacade.createUserReport(username1,username2,description);
        reportsFacade.createVolunteeringPostReport(username1,volunteeringPostId,description);
        reportsFacade.createVolunteerPostReport(username1,volunteerPostId,description);
        reportsFacade.createOrganizationReport(username1,organizationId,description);
        reportsFacade.createVolunteeringReport(username1,volunteeringId,description);

        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getAllReportDTOs(username1)); // ensure all reports are returned
    }

    @Test
    void givenAdmin_whenGetAllUserReports_thenReturnsAllUserReports() {
        // Setup - create some reports
        String description_base = "givenAdmin_whenGetAllUserReports_thenReturnsAllUserReports";
        String[] usernames = new String[]{username2,username3,username4};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createUserReport(username1,usernames[i],descriptions[i]);
        }

        List<ReportDTO> reports = reportsFacade.getAllUserReports(adminUsername); // ensure all reports are returned
        Assertions.assertEquals(3, reports.size());
        for (int i = 0; i < descriptions.length; i++) {
            final int index = i; // because it has to be final inside lambda expression
            Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(usernames[index])
                    && r.getReportingUser().equals(username1)
                    && r.getDescription().equals(descriptions[index])));
        }
    }

    @Test
    void givenNotAdmin_whenGetAllUserReports_thenError() {
        // Setup - create some reports
        String description_base = "givenNotAdmin_whenGetAllUserReports_thenError";
        String[] usernames = new String[]{username2,username3,username4};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createUserReport(username1,usernames[i],descriptions[i]);
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getAllUserReports(username1)); // ensure reports are not returned when not admin
    }

    @Test
    void givenAdmin_whenGetAllVolunteeringPostReports_thenReturnsAllVolunteeringPostReports() {
        // Setup - create some reports
        String description_base = "givenAdmin_whenGetAllVolunteeringPostReports_thenReturnsAllVolunteeringPostReports";
        int[] postIds = new int[]{volunteeringPostId,volunteeringPostId2,volunteeringPostId3};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createVolunteeringPostReport(username1,postIds[i],descriptions[i]);
        }

        List<ReportDTO> reports = reportsFacade.getAllVolunteeringPostReports(adminUsername); // ensure all reports are returned
        Assertions.assertEquals(3, reports.size());
        for (int i = 0; i < descriptions.length; i++) {
            final int index = i; // because it has to be final inside lambda expression
            Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(postIds[index]+"")
                    && r.getReportingUser().equals(username1)
                    && r.getDescription().equals(descriptions[index])));
        }
    }

    @Test
    void givenNotAdmin_whenGetAllVolunteeringPostReports_thenError() {
        // Setup - create some reports
        String description_base = "givenNotAdmin_whenGetAllVolunteeringPostReports_thenError";
        int[] postIds = new int[]{volunteeringPostId,volunteeringPostId2,volunteeringPostId3};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createVolunteeringPostReport(username1,postIds[i],descriptions[i]);
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getAllVolunteeringPostReports(username1)); // ensure reports are not returned when not admin
    }

    @Test
    void givenAdmin_whenGetAllVolunteerPostReports_thenReturnsAllVolunteerPostReports() {
        // Setup - create some reports
        String description_base = "givenAdmin_whenGetAllVolunteerPostReports_thenReturnsAllVolunteerPostReports";
        int[] postIds = new int[]{volunteerPostId,volunteerPostId2,volunteerPostId3};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createVolunteerPostReport(username1,postIds[i],descriptions[i]);
        }

        List<ReportDTO> reports = reportsFacade.getAllVolunteerPostReports(adminUsername); // ensure all reports are returned
        Assertions.assertEquals(3, reports.size());
        for (int i = 0; i < descriptions.length; i++) {
            final int index = i; // because it has to be final inside lambda expression
            Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(postIds[index]+"")
                    && r.getReportingUser().equals(username1)
                    && r.getDescription().equals(descriptions[index])));
        }
    }

    @Test
    void givenNotAdmin_whenGetAllVolunteerPostReports_thenError() {
        // Setup - create some reports
        String description_base = "givenNotAdmin_whenGetAllVolunteerPostReports_thenError";
        int[] postIds = new int[]{volunteerPostId,volunteerPostId2,volunteerPostId3};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createVolunteerPostReport(username1,postIds[i],descriptions[i]);
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getAllVolunteerPostReports(username1)); // ensure reports are not returned when not admin
    }

    @Test
    void givenAdmin_whenGetAllOrganizationReports_thenReturnsAllOrganizationReports() {
        // Setup - create some reports
        String description_base = "givenAdmin_whenGetAllOrganizationReports_thenReturnsAllOrganizationReports";
        int[] orgIds = new int[]{organizationId,organizationId2,organizationId3};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createOrganizationReport(username1,orgIds[i],descriptions[i]);
        }

        List<ReportDTO> reports = reportsFacade.getAllOrganizationReports(adminUsername); // ensure all reports are returned
        Assertions.assertEquals(3, reports.size());
        for (int i = 0; i < descriptions.length; i++) {
            final int index = i; // because it has to be final inside lambda expression
            Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(orgIds[index]+"")
                    && r.getReportingUser().equals(username1)
                    && r.getDescription().equals(descriptions[index])));
        }
    }

    @Test
    void givenNotAdmin_whenGetAllOrganizationReports_thenError() {
        // Setup - create some reports
        String description_base = "givenNotAdmin_whenGetAllOrganizationReports_thenError";
        int[] orgIds = new int[]{organizationId,organizationId2,organizationId3};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createOrganizationReport(username1,orgIds[i],descriptions[i]);
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getAllOrganizationReports(username1)); // ensure reports are not returned when not admin
    }

    @Test
    void givenAdmin_whenGetAllVolunteeringReports_thenReturnsAllVolunteeringReports() {
        // Setup - create some reports
        String description_base = "givenAdmin_whenGetAllVolunteeringReports_thenReturnsAllVolunteeringReports";
        int[] volunteeringIds = new int[]{volunteeringId,volunteeringId2,volunteeringId3};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createVolunteeringReport(username1,volunteeringIds[i],descriptions[i]);
        }

        List<ReportDTO> reports = reportsFacade.getAllVolunteeringReports(adminUsername); // ensure all reports are returned
        Assertions.assertEquals(3, reports.size());
        for (int i = 0; i < descriptions.length; i++) {
            final int index = i; // because it has to be final inside lambda expression
            Assertions.assertTrue(reports.stream().anyMatch(r -> r.getReportedId().equals(volunteeringIds[index]+"")
                    && r.getReportingUser().equals(username1)
                    && r.getDescription().equals(descriptions[index])));
        }
    }

    @Test
    void givenNotAdmin_whenGetAllVolunteeringReports_thenError() {
        // Setup - create some reports
        String description_base = "givenNotAdmin_whenGetAllOrganizationReports_thenError";
        int[] volunteeringIds = new int[]{volunteeringId,volunteeringId2,volunteeringId3};
        String[] descriptions = new String[3];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = description_base + i;
            reportsFacade.createVolunteeringReport(username1,volunteeringIds[i],descriptions[i]);
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> reportsFacade.getAllVolunteeringReports(username1)); // ensure reports are not returned when not admin
    }

    // Note: don't need to test edit/get
}