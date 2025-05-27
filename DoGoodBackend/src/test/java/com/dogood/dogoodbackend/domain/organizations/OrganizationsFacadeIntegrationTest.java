package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.domain.reports.*;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.FacadeManager;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.ReportErrors;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrganizationsFacadeIntegrationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FacadeManager facadeManager;

    private int organizationId, volunteeringId;
    private final String name1 = "Magen David Adom";
    private final String description1 = "Magen David Adom is Israel's national emergency medical and blood services organization.";
    private final String phoneNumber1 = "0548124087";
    private final String email1 = "mada@gmail.com";
    private final String name2 = "Magen David Kachol";
    private final String description2 = "Water services";
    private final String phoneNumber2 = "0547424087";
    private final String email2 = "madk@gmail.com";
    private final String founder = "TheDoctor";
    private final String manager = "NotTheDoctor";
    private final String anotherUser = "NotNotTheDoctor";
    private final String newUser = "newUser";
    private final String anotherAnotherUser = "NotNotNotTheDoctor";
    private final String admin = "Admin";

    private OrganizationsFacade organizationsFacade;
    private RequestRepository requestRepository;
    private UsersFacade usersFacade;
    private VolunteeringFacade volunteeringFacade;
    private ReportsFacade reportsFacade;
    private NotificationSystem notificationSystem;

    @Mock
    private NotificationSocketSender sender;

    @BeforeEach
    void setUp() {
        this.organizationsFacade = facadeManager.getOrganizationsFacade();
        this.requestRepository = facadeManager.getPostsFacade().getRequestRepository();
        this.usersFacade = facadeManager.getUsersFacade();
        this.volunteeringFacade = facadeManager.getVolunteeringFacade();
        this.reportsFacade = facadeManager.getReportsFacade();
        this.notificationSystem = facadeManager.getNotificationSystem();

        this.notificationSystem.setSender(sender);
        this.organizationsFacade.setReportFacade(this.reportsFacade);
        this.organizationsFacade.setNotificationSystem(this.notificationSystem);
        this.organizationsFacade.setVolunteeringFacade(this.volunteeringFacade);

        applicationContext.getBean(UserJPA.class).deleteAll();
        applicationContext.getBean(VolunteeringJPA.class).deleteAll();
        applicationContext.getBean(OrganizationJPA.class).deleteAll();
        applicationContext.getBean(VolunteeringPostJPA.class).deleteAll();
        applicationContext.getBean(VolunteerPostJPA.class).deleteAll();
        applicationContext.getBean(RequestJPA.class).deleteAll();
        applicationContext.getBean(ReportJPA.class).deleteAll();
        applicationContext.getBean(BannedJPA.class).deleteAll();
        applicationContext.getBean(HourRequestJPA.class).deleteAll();
        applicationContext.getBean(AppointmentJPA.class).deleteAll();
        applicationContext.getBean(MessageJPA.class).deleteAll();
        applicationContext.getBean(NotificationJPA.class).deleteAll();

        usersFacade.register(founder, "password", "actor 1", "actor1@gmail.com", "0541980654", new Date());
        usersFacade.register(manager, "password", "actor 2", "actor2@gmail.com", "0541980654", new Date());
        usersFacade.register(anotherUser, "password", "actor 3", "actor3@gmail.com", "0541980654", new Date());
        usersFacade.register(anotherAnotherUser, "password", "actor 4", "actor4@gmail.com", "0541980654", new Date());
        usersFacade.registerAdmin(admin, "password", "admin", "admin@gmail.com", "0541980654", new Date());
        this.organizationId = this.organizationsFacade.createOrganization(name1, description1, phoneNumber1, email1, founder);
        organizationsFacade.sendAssignManagerRequest(manager, founder, organizationId);
        organizationsFacade.handleAssignManagerRequest(manager, organizationId, true);
        this.volunteeringId = organizationsFacade.createVolunteering(organizationId, "Volunteering", "Description", founder);

        reset(sender);
    }

    @Test
    void givenValidFields_whenCreateOrganization_thenCreate() {
        assertEquals(1, organizationsFacade.getAllOrganizations().size());
        int newOrgId = organizationsFacade.createOrganization(name2, description2, phoneNumber2, email2, founder);
        assertEquals(2, organizationsFacade.getAllOrganizations().size());

        OrganizationDTO newOrg = organizationsFacade.getOrganization(newOrgId);
        OrganizationDTO expected = new OrganizationDTO(newOrgId, name2, description2, phoneNumber2, email2, new ArrayList<>(), List.of(founder), founder, new ArrayList<>(), null);
        assertEquals(expected, newOrg);
    }

    @Test
    void givenInvalidFields_whenCreateOrganization_thenThrowException() {
        assertEquals(1, organizationsFacade.getAllOrganizations().size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.createOrganization("", "", "", "", founder);
        });
        StringBuilder expectedError = new StringBuilder();
        expectedError
                .append("Invalid organization name: .\n")
                .append("Invalid organization description: .\n")
                .append("Invalid phone number: .\n")
                .append("Invalid email: .");

        assertEquals(expectedError.toString(), exception.getMessage());

        assertEquals(1, organizationsFacade.getAllOrganizations().size());
    }

    @Test
    void givenNonExistingUser_whenCreateOrganization_thenThrowException() {
        assertEquals(1, organizationsFacade.getAllOrganizations().size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.createOrganization(name1, description1, phoneNumber1, email1, newUser);
        });
        assertEquals("User " + newUser + " doesn't exist", exception.getMessage());

        assertEquals(1, organizationsFacade.getAllOrganizations().size());
    }

    private void verifyMessage(String message, List<String> usersnames) {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(sender).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        verify(sender, times(usersnames.size())).sendNotification(recipientCaptor.capture(), captor.capture());

        List<String> recipients = recipientCaptor.getAllValues();
        List<Notification> notifications = captor.getAllValues();

        assertTrue(recipients.containsAll(usersnames));
        for (Notification notification : notifications) {
            assertEquals(message, notification.getMessage());
        }
    }

    public void verifyOrganizationExists(int organizationId, boolean exists) {
        if(exists) {
            assertDoesNotThrow(() -> organizationsFacade.getOrganization(organizationId));
        }
        else {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                organizationsFacade.getOrganization(organizationId);
            });
            assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId), exception.getMessage());
        }
    }

    public void verifyVolunteeringExists(int volunteeringId, boolean exists) {
        if(exists) {
            assertDoesNotThrow(() -> volunteeringFacade.getVolunteeringDTO(volunteeringId));
        }
        else {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                volunteeringFacade.getVolunteeringDTO(volunteeringId);
            });
            assertEquals("Volunteering with id " + volunteeringId + " does not exist", exception.getMessage());
        }
    }

    public void verifyRequestExists(String assignee, int organizationId, boolean exists) {
        if(exists) {
            assertDoesNotThrow(() -> requestRepository.getRequest(assignee, organizationId, RequestObject.ORGANIZATION));
        }
        else {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                requestRepository.getRequest(assignee, organizationId, RequestObject.ORGANIZATION);
            });
            String message = String.format("A request to assign %s to %s with id %d does not exist.", assignee, "organization", organizationId);
            assertEquals(message, exception.getMessage());
        }
    }

    public void verifyReportExists(String reporting, LocalDate date, int organizationId, boolean exists) {
        if(exists) {
            assertDoesNotThrow(() -> reportsFacade.getOrganizationReport(reporting, date, organizationId, reporting));
        }
        else {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                reportsFacade.getOrganizationReport(reporting, date, organizationId, reporting);
            });
            ReportKey key = new ReportKey(reporting, date, organizationId + "", ReportObject.ORGANIZATION);
            assertEquals(ReportErrors.makeReportDoesNotExistError(key), exception.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Admin", "TheDoctor"})
    void givenExistingIdAndFounderOrAdmin_whenRemoveOrganization_thenRemove(String validActor) {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        ReportDTO report = reportsFacade.createOrganizationReport(anotherUser, organizationId, "Bad Organization");

        reset(sender);

        verifyOrganizationExists(organizationId, true);
        verifyVolunteeringExists(volunteeringId, true);
        verifyRequestExists(anotherUser, organizationId, true);
        verifyReportExists(anotherUser, report.getDate(), organizationId, true);
        assertTrue(organizationsFacade.getOrganization(organizationId).getVolunteeringIds().contains(volunteeringId));

        organizationsFacade.removeOrganization(organizationId, validActor);

        verifyOrganizationExists(organizationId, false);
        verifyVolunteeringExists(volunteeringId, false);
        verifyRequestExists(anotherUser, organizationId, false);
        verifyReportExists(anotherUser, report.getDate(), organizationId, false);

        String removeOrg = String.format("Your organization \"%s\" was removed.", name1);
        String removeVolunteering = String.format("The volunteering \"%s\" was removed from your organization \"%s\".", "Volunteering", name1);
        List<String> managers = List.of(founder, manager);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(sender).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));

        verify(sender, times(4)).sendNotification(recipientCaptor.capture(), captor.capture());

        List<String> recipients = recipientCaptor.getAllValues();
        List<Notification> notifications = captor.getAllValues();

        assertTrue(recipients.subList(0, 2).containsAll(managers));
        for (Notification notification : notifications.subList(0, 2)) {
            assertEquals(removeVolunteering, notification.getMessage());
        }

        assertTrue(recipients.subList(2, 4).containsAll(managers));
        for (Notification notification : notifications.subList(2, 4)) {
            assertEquals(removeOrg, notification.getMessage());
        }
    }

    @Test
    void givenExistingIdAndManager_whenRemoveOrganization_thenThrowException() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        ReportDTO report = reportsFacade.createOrganizationReport(anotherUser, organizationId, "Bad Organization");

        reset(sender);

        verifyOrganizationExists(organizationId, true);
        verifyVolunteeringExists(volunteeringId, true);
        verifyRequestExists(anotherUser, organizationId, true);
        verifyReportExists(anotherUser, report.getDate(), organizationId, true);
        assertTrue(organizationsFacade.getOrganization(organizationId).getVolunteeringIds().contains(volunteeringId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeOrganization(organizationId, manager);
        });
        assertEquals(OrganizationErrors.makeNonFounderCanNotPreformActionError(manager, name1, "remove the organization"), exception.getMessage());

        verifyOrganizationExists(organizationId, true);
        verifyVolunteeringExists(volunteeringId, true);
        verifyRequestExists(anotherUser, organizationId, true);
        verifyReportExists(anotherUser, report.getDate(), organizationId, true);
        assertTrue(organizationsFacade.getOrganization(organizationId).getVolunteeringIds().contains(volunteeringId));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingUser_whenRemoveOrganization_thenThrowException() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        ReportDTO report = reportsFacade.createOrganizationReport(anotherUser, organizationId, "Bad Organization");

        reset(sender);

        verifyOrganizationExists(organizationId, true);
        verifyVolunteeringExists(volunteeringId, true);
        verifyRequestExists(anotherUser, organizationId, true);
        verifyReportExists(anotherUser, report.getDate(), organizationId, true);
        assertTrue(organizationsFacade.getOrganization(organizationId).getVolunteeringIds().contains(volunteeringId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeOrganization(organizationId, newUser);
        });
        assertEquals("User " + newUser + " doesn't exist", exception.getMessage());

        verifyOrganizationExists(organizationId, true);
        verifyVolunteeringExists(volunteeringId, true);
        verifyRequestExists(anotherUser, organizationId, true);
        verifyReportExists(anotherUser, report.getDate(), organizationId, true);
        assertTrue(organizationsFacade.getOrganization(organizationId).getVolunteeringIds().contains(volunteeringId));
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingId_whenRemoveOrganization_thenThrowException() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        ReportDTO report = reportsFacade.createOrganizationReport(anotherUser, organizationId, "Bad Organization");

        reset(sender);

        verifyOrganizationExists(organizationId, true);
        verifyVolunteeringExists(volunteeringId, true);
        verifyRequestExists(anotherUser, organizationId, true);
        verifyReportExists(anotherUser, report.getDate(), organizationId, true);
        assertTrue(organizationsFacade.getOrganization(organizationId).getVolunteeringIds().contains(volunteeringId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeOrganization(organizationId + 1, founder);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());

        verifyOrganizationExists(organizationId, true);
        verifyVolunteeringExists(volunteeringId, true);
        verifyRequestExists(anotherUser, organizationId, true);
        verifyReportExists(anotherUser, report.getDate(), organizationId, true);
        assertTrue(organizationsFacade.getOrganization(organizationId).getVolunteeringIds().contains(volunteeringId));
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TheDoctor", "NotTheDoctor", "Admin"})
    void givenExistingIdAndFounderAndValidFields_whenEditOrganization_thenEdit(String validUser) {
        OrganizationDTO expectedBefore = new OrganizationDTO(organizationId, name1, description1, phoneNumber1, email1, List.of(volunteeringId), List.of(founder, manager), founder, new ArrayList<>(), null);
        OrganizationDTO orgBefore = organizationsFacade.getOrganization(organizationId);
        assertEquals(expectedBefore, orgBefore);

        organizationsFacade.editOrganization(organizationId, name2, description2, phoneNumber2, email2, validUser);

        OrganizationDTO expectedAfter = new OrganizationDTO(organizationId, name2, description2, phoneNumber2, email2, List.of(volunteeringId), List.of(founder, manager), founder, new ArrayList<>(), null);
        OrganizationDTO orgAfter = organizationsFacade.getOrganization(organizationId);
        assertEquals(expectedAfter, orgAfter);

        verifyMessage(String.format("Your organization \"%s\" was edited.", name1), List.of(founder, manager));
    }

    @Test
    void givenNonManager_whenEditOrganization_thenThrowException() {
        OrganizationDTO expected = new OrganizationDTO(organizationId, name1, description1, phoneNumber1, email1, List.of(volunteeringId), List.of(founder, manager), founder, new ArrayList<>(), null);
        OrganizationDTO orgBefore = organizationsFacade.getOrganization(organizationId);
        assertEquals(expected, orgBefore);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.editOrganization(organizationId, name2, description2, phoneNumber2, email2, anotherUser);
        });
        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(anotherUser, name1, "edit the organization's details"), exception.getMessage());

        OrganizationDTO orgAfter = organizationsFacade.getOrganization(organizationId);
        assertEquals(expected, orgAfter);

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingId_whenEditOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.editOrganization(organizationId + 1, name2, description2, phoneNumber2, email2, founder);
        });

        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenInvalidFields_whenEditOrganization_thenThrowException() {
        OrganizationDTO expected = new OrganizationDTO(organizationId, name1, description1, phoneNumber1, email1, List.of(volunteeringId), List.of(founder, manager), founder, new ArrayList<>(), null);
        OrganizationDTO orgBefore = organizationsFacade.getOrganization(organizationId);
        assertEquals(expected, orgBefore);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.editOrganization(organizationId, "", "", "", "", founder);
        });

        StringBuilder expectedError = new StringBuilder();
        expectedError
                .append("Invalid organization name: .\n")
                .append("Invalid organization description: .\n")
                .append("Invalid phone number: .\n")
                .append("Invalid email: .");
        assertEquals(expectedError.toString(), exception.getMessage());

        OrganizationDTO orgAfter = organizationsFacade.getOrganization(organizationId);
        assertEquals(expected, orgAfter);

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TheDoctor", "NotTheDoctor"})
    void givenManager_whenCreateVolunteering_thenCreate(String validUser) {
        int volunteeringId = organizationsFacade.createVolunteering(organizationId, "Volunteering", "Description", validUser);
        verifyVolunteeringExists(volunteeringId, true);
        assertTrue(organizationsFacade.getOrganization(organizationId).getVolunteeringIds().contains(volunteeringId));

        String message = String.format("A new volunteering \"%s\" was added to your organization \"%s\".", "Volunteering", name1);
        verifyMessage(message, List.of(founder, manager));
    }

    @Test
    void givenNonExistingUser_whenCreateVolunteering_thenThrowException() {
        assertEquals(new HashSet<>(List.of(volunteeringId)), new HashSet<>(organizationsFacade.getOrganization(organizationId).getVolunteeringIds()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.createOrganization(name1, description1, phoneNumber1, email1, newUser);
        });
        assertEquals("User " + newUser + " doesn't exist", exception.getMessage());

        assertEquals(new HashSet<>(List.of(volunteeringId)), new HashSet<>(organizationsFacade.getOrganization(organizationId).getVolunteeringIds()));
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonManager_whenCreateVolunteering_thenThrowException() {
        assertEquals(new HashSet<>(List.of(volunteeringId)), new HashSet<>(organizationsFacade.getOrganization(organizationId).getVolunteeringIds()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.createVolunteering(organizationId, "Volunteering", "Description", anotherUser);
        });
        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(anotherUser, name1, "create a new volunteering"), exception.getMessage());

        assertEquals(new HashSet<>(List.of(volunteeringId)), new HashSet<>(organizationsFacade.getOrganization(organizationId).getVolunteeringIds()));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingOrganization_whenCreateVolunteering_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.createVolunteering(organizationId + 1, "Volunteering", "Description", manager);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TheDoctor", "NotTheDoctor", "Admin"})
    void givenManagerAndExistingVolunteering_whenRemoveVolunteering_thenRemoveVolunteering(String validUser) {
        assertEquals(new HashSet<>(List.of(volunteeringId)), new HashSet<>(organizationsFacade.getOrganization(organizationId).getVolunteeringIds()));
        verifyVolunteeringExists(volunteeringId, true);

        organizationsFacade.removeVolunteering(organizationId, volunteeringId, validUser);

        assertEquals(new HashSet<>(), new HashSet<>(organizationsFacade.getOrganization(organizationId).getVolunteeringIds()));

        String message = String.format("The volunteering \"%s\" was removed from your organization \"%s\".", "Volunteering", name1);
        verifyMessage(message, List.of(founder, manager));
    }

    @Test
    void givenNonExistingUser_whenRemoveVolunteering_thenThrowException() {
        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
        verifyVolunteeringExists(volunteeringId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId, volunteeringId, newUser);
        });
        assertEquals("User " + newUser + " doesn't exist", exception.getMessage());

        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
        verifyVolunteeringExists(volunteeringId, true);

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonManager_whenRemoveVolunteering_thenThrowException() {
        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
        verifyVolunteeringExists(volunteeringId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId, volunteeringId, anotherUser);
        });
        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(anotherUser, name1, "remove a volunteering"), exception.getMessage());

        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
        verifyVolunteeringExists(volunteeringId, true);

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingVolunteering_whenRemoveVolunteering_thenThrowException() {
        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId, volunteeringId + 1, founder);
        });
        assertEquals("Volunteering with id " + (volunteeringId + 1) + " does not exist", exception.getMessage());

        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingOrganization_whenRemoveVolunteering_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId + 1, 0, manager);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenVolunteeringInAnotherOrganization_whenRemoveVolunteering_thenThrowException() {
        int orgId2 = organizationsFacade.createOrganization(name2, description2, phoneNumber2, email2, manager);
        int volunteeringId2 = organizationsFacade.createVolunteering(orgId2, "Volunteering", "Description", manager);

        reset(sender);

        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
        verifyVolunteeringExists(volunteeringId, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeVolunteering(organizationId, volunteeringId2, founder);
        });
        assertEquals(OrganizationErrors.makeVolunteeringDoesNotExistsError(volunteeringId2, name1), exception.getMessage());

        assertEquals(List.of(volunteeringId), organizationsFacade.getOrganization(organizationId).getVolunteeringIds());
        verifyVolunteeringExists(volunteeringId, true);

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenManagerAssigningNonManager_whenSendAssignManagerRequest_thenSendRequest() {
        verifyRequestExists(anotherUser, organizationId, false);

        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);

        verifyRequestExists(anotherUser, organizationId, true);

        String message = String.format("%s is asking you to be manager of organization \"%s\".", founder, name1);
        verifyMessage(message, List.of(anotherUser));
    }

    @Test
    void givenNonExistingAssignee_whenSendAssignManagerRequest_thenThrowException() {
        verifyRequestExists(newUser, organizationId, false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(newUser, founder, organizationId);
        });
        assertEquals("User " + newUser + " doesn't exist", exception.getMessage());

        verifyRequestExists(newUser, organizationId, false);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingAssigner_whenSendAssignManagerRequest_thenThrowException() {
        verifyRequestExists(anotherUser, organizationId, false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(anotherUser, newUser, organizationId);
        });
        assertEquals("User " + newUser + " doesn't exist", exception.getMessage());

        verifyRequestExists(anotherUser, organizationId, false);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonManagerPreformingAction_whenSendAssignManagerRequest_thenThrowException() {
        verifyRequestExists(anotherUser, organizationId, false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(anotherAnotherUser, anotherUser, organizationId);
        });
        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(anotherUser, name1, "send assign manager request"), exception.getMessage());

        verifyRequestExists(anotherUser, organizationId, false);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenAssigningVolunteer_whenSendAssignManagerRequest_thenThrowException() {
        verifyRequestExists(anotherUser, organizationId, false);

        volunteeringFacade.requestToJoinVolunteering(anotherUser, volunteeringId, "Please");
        volunteeringFacade.acceptUserJoinRequest(founder, volunteeringId, anotherUser, 0);

        reset(sender);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        });

        assertEquals(OrganizationErrors.makeUserIsVolunteerInTheOrganizationError(anotherUser, name1), exception.getMessage());

        verifyRequestExists(anotherUser, organizationId, false);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenManagerAssigned_whenSendAssignManagerRequest_thenThrowException() {
        verifyRequestExists(anotherUser, organizationId, false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(manager, founder, organizationId);
        });
        assertEquals(OrganizationErrors.makeUserIsAlreadyAManagerError(manager, name1), exception.getMessage());

        verifyRequestExists(anotherUser, organizationId, false);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenDoubleRequest_whenSendAssignManagerRequest_thenSendRequest() {
        verifyRequestExists(anotherUser, organizationId, false);
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        verifyRequestExists(anotherUser, organizationId, true);

        reset(sender);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        });
        assertEquals("A request to assign " + anotherUser +" to organization with id " + organizationId + " already exists.", exception.getMessage());

        verifyRequestExists(anotherUser, organizationId, true);
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenAcceptingRequest_whenHandleAssignManagerRequest_thenAddManager() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        verifyRequestExists(anotherUser, organizationId, true);

        reset(sender);

        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));

        organizationsFacade.handleAssignManagerRequest(anotherUser, organizationId, true);
        verifyRequestExists(anotherUser, organizationId, false);

        assertTrue(organizationsFacade.isManager(anotherUser, organizationId));

        String message = String.format("%s has %s your assign as manager request.", anotherUser, "approved");
        verifyMessage(message, List.of(founder));
    }

    @Test
    void givenDenyingRequest_whenHandleAssignManagerRequest_thenDoNothing() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        verifyRequestExists(anotherUser, organizationId, true);

        reset(sender);

        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));

        organizationsFacade.handleAssignManagerRequest(anotherUser, organizationId, false);
        verifyRequestExists(anotherUser, organizationId, false);
        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));

        String message = String.format("%s has %s your assign as manager request.", anotherUser, "denied");
        verifyMessage(message, List.of(founder));
    }

    @Test
    void givenNonExistingUser_whenHandleAssignManagerRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.handleAssignManagerRequest(newUser, organizationId, true);
        });
        assertEquals("User " + newUser + " doesn't exist", exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNoRequest_whenHandleAssignManagerRequest_thenThrowException() {
        verifyRequestExists(anotherUser, organizationId, false);

        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.handleAssignManagerRequest(anotherUser, organizationId, true);
        });
        assertEquals("A request to assign NotNotTheDoctor to organization with id " + organizationId + " does not exist.", exception.getMessage());

        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenManager_whenResign_thanRemoveManager() {
        assertTrue(organizationsFacade.isManager(manager, organizationId));

        organizationsFacade.resign(manager, organizationId);

        assertFalse(organizationsFacade.isManager(manager, organizationId));

        String message = String.format("The manager \"%s\" resigned from your organization \"%s\".", manager, name1);
        verifyMessage(message, List.of(founder));
    }

    @Test
    void givenNonExistingUser_whenResign_thanThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.resign(newUser, organizationId);
        });
        assertEquals("User " + newUser + " doesn't exist", exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenFounder_whenResign_thanThrowException() {
        assertTrue(organizationsFacade.isManager(founder, organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.resign(founder, organizationId);
        });
        assertEquals(OrganizationErrors.makeFounderCanNotResignError(founder, name1), exception.getMessage());
        assertTrue(organizationsFacade.isManager(founder, organizationId));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonManager_whenResign_thanThrowException() {
        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.resign(anotherUser, organizationId);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(anotherUser, name1), exception.getMessage());

        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingOrganization_whenResign_thanThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.resign(manager, organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenFounderRemovingManager_whenRemoveManager_thenRemove() {
        assertTrue(organizationsFacade.isManager(manager, organizationId));

        organizationsFacade.removeManager(founder, manager, organizationId);

        assertFalse(organizationsFacade.isManager(manager, organizationId));

        verifyMessage(String.format("You are no longer a manager of organization \"%s\".", name1), List.of(manager));
    }

    @Test
    void givenNonExistingActor_whenRemoveManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(newUser, manager, organizationId + 1);
        });
        assertEquals("User newUser doesn't exist", exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingRemovedUser_whenRemoveManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(founder, newUser, organizationId + 1);
        });
        assertEquals("User newUser doesn't exist", exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenManagerNonFounderPreformingAction_whenRemoveManager_thenThrowException() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        organizationsFacade.handleAssignManagerRequest(anotherUser, organizationId, true);

        reset(sender);

        assertTrue(organizationsFacade.isManager(anotherUser, organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(manager, anotherUser, organizationId);
        });
        assertEquals(OrganizationErrors.makeNonFounderCanNotPreformActionError(manager, name1, "remove a manager from organization"), exception.getMessage());

        assertTrue(organizationsFacade.isManager(anotherUser, organizationId));
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenRemovingFounder_whenRemoveManager_thenThrowException() {
        assertTrue(organizationsFacade.isManager(founder, organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(founder, founder, organizationId);
        });
        assertEquals(OrganizationErrors.makeFounderCanNotBeRemovedError(founder, name1), exception.getMessage());

        assertTrue(organizationsFacade.isManager(founder, organizationId));
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenRemovingNonManager_whenRemoveManager_thenThrowException() {
        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(founder, anotherUser, organizationId);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(anotherUser, name1), exception.getMessage());

        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingOrganization_whenRemoveManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.removeManager(founder, manager, organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenFounderSettingManager_whenSetFounder_thenSet() {
        OrganizationDTO organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(founder, organization.getFounderUsername());

        organizationsFacade.setFounder(founder, manager, organizationId);

        organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(manager, organization.getFounderUsername());

        String message = String.format("%s is the new founder of your organization \"%s\".", manager, name1);
        verifyMessage(message, List.of(founder, manager));
    }

    @Test
    void givenNonExistingActor_whenSetFounder_thenSet() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(newUser, manager, organizationId);
        });
        assertEquals("User newUser doesn't exist", exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingNewFounder_whenSetFounder_thenSet() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(founder, newUser, organizationId);
        });
        assertEquals("User newUser doesn't exist", exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenFounderSettingNonManager_whenSetFounder_thenThrowException() {
        OrganizationDTO organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(founder, organization.getFounderUsername());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(founder, anotherUser, organizationId);
        });
        assertEquals(OrganizationErrors.makeUserIsNotAManagerError(anotherUser, name1), exception.getMessage());

        organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(founder, organization.getFounderUsername());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonFounderSettingManager_whenSetFounder_thenThrowException() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        organizationsFacade.handleAssignManagerRequest(anotherUser, organizationId, true);

        reset(sender);

        OrganizationDTO organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(founder, organization.getFounderUsername());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(manager, anotherUser, organizationId);
        });
        assertEquals(OrganizationErrors.makeNonFounderCanNotPreformActionError(manager, name1, "set a new founder to the organization"), exception.getMessage());

        organization = organizationsFacade.getOrganization(organizationId);
        assertEquals(founder, organization.getFounderUsername());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNonExistingOrganization_whenSetFounder_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(founder, manager, organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenFounderOfAnotherOrganization_whenSetFounder_thenThrowException() {
        organizationsFacade.createOrganization(name2, description2, phoneNumber2, email2, anotherUser);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.setFounder(anotherUser, manager, organizationId);
        });
        assertEquals(OrganizationErrors.makeNonFounderCanNotPreformActionError(anotherUser, name1, "set a new founder to the organization"), exception.getMessage());

        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenNoRequests_whenGetUserRequests_thenReturnEmptyList() {
        assertEquals(new ArrayList<>(), organizationsFacade.getUserRequests(anotherUser));
    }

    @Test
    void givenApprovedRequest_whenGetUserRequests_thenReturnEmptyList() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);
        organizationsFacade.handleAssignManagerRequest(anotherUser, organizationId, true);
        assertEquals(new ArrayList<>(), organizationsFacade.getUserRequests(anotherUser));
    }

    @Test
    void givenPendingRequest_whenGetUserRequests_thenReturnList() {
        organizationsFacade.sendAssignManagerRequest(anotherUser, founder, organizationId);

        List<Request> res = organizationsFacade.getUserRequests(anotherUser);
        List<Request> expected = List.of(new Request(anotherUser, founder, organizationId, RequestObject.ORGANIZATION));
        expected.get(0).setDate(res.get(0).getDate());
        assertEquals(expected, res);
    }

    @Test
    void givenNonExistingNewFounder_whenGetUserRequests_thenSet() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.getUserRequests(newUser);
        });
        assertEquals("User newUser doesn't exist", exception.getMessage());
    }

    @Test
    void givenExistingOrganization_whenGetOrganization_thenReturnOrganization() {
        OrganizationDTO expected = new OrganizationDTO(organizationId, name1, description1, phoneNumber1, email1, List.of(volunteeringId), List.of(founder, manager), founder, new ArrayList<>(), null);
        assertEquals(expected, organizationsFacade.getOrganization(organizationId));
    }

    @Test
    void givenNonExistingOrganization_whenGetOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.getOrganization(organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void getAllOrganizations() {
        OrganizationDTO expected = new OrganizationDTO(organizationId, name1, description1, phoneNumber1, email1, List.of(volunteeringId), List.of(founder, manager), founder, new ArrayList<>(), null);
        assertEquals(List.of(expected), organizationsFacade.getAllOrganizations());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TheDoctor", "NotTheDoctor"})
    void givenManager_whenIsManager_thenReturnTrue(String manager) {
        assertTrue(organizationsFacade.isManager(manager, organizationId));
    }

    @Test
    void givenNonManager_whenIsManager_thenReturnFalse() {
        assertFalse(organizationsFacade.isManager(anotherUser, organizationId));
    }

    @Test
    void givenNonExistingUser_whenIsManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.isManager(newUser, organizationId);
        });
        assertEquals("User newUser doesn't exist", exception.getMessage());
    }

    @Test
    void givenNonExistingOrganization_whenIsManager_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.isManager(founder, organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenVolunteerings_whenGetOrganizationVolunteerings_thenReturnList() {
        List<Integer> res = organizationsFacade.getOrganization(organizationId).getVolunteeringIds();
        assertEquals(List.of(volunteeringId), res);
    }

    @Test
    void givenNonExistingOrganization_whenGetOrganizationVolunteerings_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.getOrganizationVolunteerings(organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenExistingOrganization_whenNotifyManagers_thenNotify() {
        String message = "Very important message!";
        organizationsFacade.notifyManagers(message, "", organizationId);
        verifyMessage(message, List.of(founder, manager));
    }

    @Test
    void givenNonExistingOrganization_whenNotifyManagers_thenThrowException() {
        String message = "Very important message!";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.notifyManagers(message, "", organizationId + 1);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
        verify(sender, times(0)).sendNotification(Mockito.anyString(), Mockito.any(Notification.class));
    }

    @Test
    void givenExistingOrganization_whenGetUserVolunteerings_thenReturnVolunteerings() {
        int volunteeringId2 = organizationsFacade.createVolunteering(organizationId, "Volunteering2", "Description2", founder);
        volunteeringFacade.requestToJoinVolunteering(anotherUser, volunteeringId, "Please");
        volunteeringFacade.acceptUserJoinRequest(founder, volunteeringId, anotherUser, 0);

        List<Integer> expected = List.of(volunteeringId);
        List<Integer> res = organizationsFacade.getUserVolunteerings(organizationId, anotherUser);
        assertEquals(expected, res);
    }

    @Test
    void givenNonExistingOrganization_whenGetUserVolunteerings_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.getUserVolunteerings(organizationId + 1, anotherUser);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenNonExistingUser_whenGetUserVolunteerings_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.getUserVolunteerings(organizationId , newUser);
        });
        assertEquals("User newUser doesn't exist", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "'TheDoctor', 'image'",
            "'TheDoctor', '\"image\"'",
            "'NotTheDoctor', 'image'",
            "'NotTheDoctor', '\"image\"'",
            "'Admin', 'image'",
            "'Admin', '\"image\"'"
    })
    void givenAddAndExistingOrganizationAndManager_whenChangeImageInOrganization_thenAddImage(String actor, String image) {
        String newImage = image;
        if(image.charAt(0) == '\"') {
            int len = image.length();
            newImage = image.substring(1, len - 1);
        }

        assertFalse(organizationsFacade.getOrganization(organizationId).getImagePaths().contains(newImage));
        organizationsFacade.changeImageInOrganization(organizationId, image, true, actor);
        assertTrue(organizationsFacade.getOrganization(organizationId).getImagePaths().contains(newImage));
    }

    @ParameterizedTest
    @CsvSource({
            "'TheDoctor', 'image'",
            "'TheDoctor', '\"image\"'",
            "'NotTheDoctor', 'image'",
            "'NotTheDoctor', '\"image\"'",
            "'Admin', 'image'",
            "'Admin', '\"image\"'"
    })
    void givenRemoveAndExistingOrganizationAndManager_whenChangeImageInOrganization_thenAddImage(String actor, String image) {
        String newImage = image;
        if(image.charAt(0) == '\"') {
            int len = image.length();
            newImage = image.substring(1, len - 1);
        }

        organizationsFacade.changeImageInOrganization(organizationId, image, true, founder);

        assertTrue(organizationsFacade.getOrganization(organizationId).getImagePaths().contains(newImage));
        organizationsFacade.changeImageInOrganization(organizationId, image, false, actor);
        assertFalse(organizationsFacade.getOrganization(organizationId).getImagePaths().contains(newImage));
    }

    @Test
    void givenNonExistingUser_whenChangeImageInOrganization_thenThrowException() {
        assertFalse(organizationsFacade.getOrganization(organizationId).getImagePaths().contains("image"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.changeImageInOrganization(organizationId , "image", true, newUser);
        });
        assertEquals("User newUser doesn't exist", exception.getMessage());

        assertFalse(organizationsFacade.getOrganization(organizationId).getImagePaths().contains("image"));
    }

    @Test
    void givenNonExistingOrganization_whenChangeImageInOrganization_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.changeImageInOrganization(organizationId + 1, "image", true, founder);
        });
        assertEquals(OrganizationErrors.makeOrganizationIdDoesNotExistError(organizationId + 1), exception.getMessage());
    }

    @Test
    void givenNonManager_whenChangeImageInOrganization_thenThrowException() {
        assertFalse(organizationsFacade.getOrganization(organizationId).getImagePaths().contains("image"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            organizationsFacade.changeImageInOrganization(organizationId, "image", true, anotherUser);
        });
        assertEquals(OrganizationErrors.makeNonManagerCanNotPreformActionError(anotherUser, name1, "add image"), exception.getMessage());

        assertFalse(organizationsFacade.getOrganization(organizationId).getImagePaths().contains("image"));
    }

    // uploadSignature & getSignature - exactly same tests as organizationRepository
}