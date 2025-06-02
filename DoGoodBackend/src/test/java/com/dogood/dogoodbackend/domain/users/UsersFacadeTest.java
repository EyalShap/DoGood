package com.dogood.dogoodbackend.domain.users;

import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.PushNotificationSender;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UsersFacadeTest {

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

    private String username;
    private int orgId;
    private int volId;
    private String volunteer;

    @BeforeEach
    public void setUp() {
        volunteeringJPA.deleteAll();
        hourRequestJPA.deleteAll();
        appointmentJPA.deleteAll();
        organizationJPA.deleteAll();
        userJPA.deleteAll();
        notificationJPA.deleteAll();

        facadeManager.getNotificationSystem().setPushNotificationSender(pushNotificationSender);
        facadeManager.getNotificationSystem().setSender(notificationSocketSender);

        usersFacade = facadeManager.getUsersFacade();
        volunteeringFacade = facadeManager.getVolunteeringFacade();
        organizationsFacade = facadeManager.getOrganizationsFacade();
        username = "User";
        volunteer = "Volunteer";
        usersFacade.register(username, "123456", "Name", "email@email.com","052-0520520",new Date());
        usersFacade.register(volunteer, "123456", "Name", "email@email.com","052-0520520",new Date());
        orgId = organizationsFacade.createOrganization("Organization", "Description", "052-0520520","email@email.com",username);
        volId = organizationsFacade.createVolunteering(orgId,"Volunteering","Description",username);
        volunteeringFacade.requestToJoinVolunteering(volunteer,volId,"");
        volunteeringFacade.acceptUserJoinRequest(username,volId,volunteer,0);
    }

    @Test
    public void givenCorrectPassword_whenLogin_thenLogin(){
        String token = usersFacade.login(username,"123456");
        Assertions.assertNotNull(token);
        Assertions.assertNotEquals("",token);
    }
    @Test
    public void givenIncorrectPassword_whenLogin_thenThrowException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> usersFacade.login(username,"1234567"));
    }
    @Test
    public void givenValid_whenRegister_thenRegister(){
        Assertions.assertDoesNotThrow(()->usersFacade.register("yossi", "1234567","yossisi",  "email@email.com", "0521231234",new Date()));
    }
    @Test
    public void givenUserAlreadyExists_whenRegister_thenThrowException(){
        usersFacade.register("yossi", "1234567","yossisi",  "email@email.com", "0521231234",new Date());
        Assertions.assertThrows(IllegalArgumentException.class,()->usersFacade.register("yossi", "1234567","yossisi",  "email@email.com", "0521231234",new Date()));
    }
    @Test
    public void givenValidToken_whenLogout_thenLogout(){
        String token = usersFacade.login(username,"123456");
        Assertions.assertDoesNotThrow(()->usersFacade.logout(token));
    }
    @Test
    public void givenInvalidToken_whenLogout_thenThrowException(){
        String token = "yossi";
        Assertions.assertThrows(io.jsonwebtoken.MalformedJwtException.class,()->usersFacade.logout(token));
    }
    @Test
    public void givenValid_whenUpdateUserFields_thenUpdate(){
        Assertions.assertDoesNotThrow(()->usersFacade.updateUserFields(username,"1234567", new LinkedList<String>(List.of("hello@gmail.com")),"yossi","052-1234321" ));
        User user = usersFacade.getUser(username);
        Assertions.assertEquals("hello@gmail.com", user.getEmails().get(0));
        Assertions.assertEquals("yossi", user.getName());
        Assertions.assertEquals("052-1234321", user.getPhone());
    }
    @Test
    public void givenInvalid_whenUpdateUserFields_thenThrows(){
        Assertions.assertThrows(IllegalArgumentException.class,()->usersFacade.updateUserFields(username,"1234567", new LinkedList<String>(List.of("hellogmailcom")),"yossi","052-1234321" ));
        User user = usersFacade.getUser(username);
        //usersFacade.register(username, "123456", "Name", "email@email.com","052-0520520",new Date());
        Assertions.assertEquals("email@email.com", user.getEmails().get(0));
        Assertions.assertEquals("Name", user.getName());
        Assertions.assertEquals("052-0520520", user.getPhone());
    }
    @Test
    public void givenValid_whenUpdateUserSkills_thenUpdateSkills(){

        Assertions.assertDoesNotThrow(()->usersFacade.updateUserSkills(username,new LinkedList<String>(List.of("java", "python"))));
        User user = usersFacade.getUser(username);
        List<String> newSkills = List.of("java", "python");
        Assertions.assertTrue(user.getSkills().containsAll(newSkills) && newSkills.containsAll(user.getSkills())); // Assert skills updated
    }
    @Test
    public void givenNonExistentUser_whenUpdateUserSkills_thenThrowException(){
        Assertions.assertThrows(IllegalArgumentException.class, ()->usersFacade.updateUserSkills("yossi",new LinkedList<String>(List.of("java", "python"))));
    }
    @Test
    public void givenInvalid_whenUpdateUserSkills_thenThrowException(){
        Assertions.assertThrows(IllegalArgumentException.class,()->usersFacade.updateUserSkills(username,null));
        User user = usersFacade.getUser(username);
        Assertions.assertTrue(user.getSkills().isEmpty());
    }


    @Test
    public void givenValid_whenUpdateUserPreferences_thenUpdatePreferences(){
        Assertions.assertDoesNotThrow(()->usersFacade.updateUserPreferences(username,new LinkedList<String>(List.of("java", "python"))));
        User user = usersFacade.getUser(username);
        Assertions.assertEquals(List.of("java", "python"), user.getPreferredCategories());
    }
    @Test
    public void givenNonExistentUser_whenUpdateUserPreferences_thenThrowException(){
        Assertions.assertThrows(IllegalArgumentException.class, ()->usersFacade.updateUserPreferences("yossi",new LinkedList<String>(List.of("java", "python"))));
    }
    @Test
    public void givenInvalid_whenUpdateUserPreferences_thenThrowException(){
        Assertions.assertThrows(IllegalArgumentException.class,()->usersFacade.updateUserPreferences(username,null));
        User user = usersFacade.getUser(username);
        Assertions.assertTrue(user.getPreferredCategories().isEmpty());
    }
    @Test
    public void givenValid_whenGetApprovedHours_thenReturnHours(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringFacade.requestHoursApproval(volunteer,volId,startDate,endDate);
        volunteeringFacade.approveUserHours(username,volId,volunteer,startDate,endDate);
        List<HourApprovalRequest> approvedHours = usersFacade.getApprovedHours(volunteer);
        Assertions.assertEquals(1,approvedHours.size());
        HourApprovalRequest approvedHour = approvedHours.get(0);
        Assertions.assertEquals(startDate.getTime(), approvedHour.getStartTime().getTime());
        Assertions.assertEquals(endDate.getTime(), approvedHour.getEndTime().getTime());
        Assertions.assertTrue(approvedHour.isApproved());
    }
    @Test
    public void givenUserDoesNotExist_whenGetApprovedHours_thenThrowException(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringFacade.requestHoursApproval(volunteer,volId,startDate,endDate);
        volunteeringFacade.approveUserHours(username,volId,volunteer,startDate,endDate);
        Assertions.assertThrows(IllegalArgumentException.class, ()->usersFacade.getApprovedHours(""));
    }


}
