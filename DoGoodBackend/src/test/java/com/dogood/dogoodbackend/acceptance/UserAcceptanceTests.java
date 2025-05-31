package com.dogood.dogoodbackend.acceptance;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.externalAIAPI.SkillsAndCategories;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.emailverification.VerificationData;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.*;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.dogood.dogoodbackend.utils.PostErrors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class UserAcceptanceTests {
    @MockitoBean
    ChatSocketSender chatSocketSender;

    //THIS IS IMPORTANT DO IT IN ALL ACCEPTANCE TESTS
    @MockitoBean
    NotificationSocketSender notificationSocketSender;

    //THIS IS ALSO VERY IMPORTANT
    @MockitoBean
    FirebaseMessaging firebaseMessaging;

    //THE INTERNET WILL BREAK IF WE DONT DO THIS IN EVERY ACCEPTANCE TEST
    @MockitoBean
    Gemini gemini;

    @MockitoBean
    EmailSender emailSender;

    @MockitoBean
    VerificationCacheService verificationCacheService;

    @Autowired
    ChatService chatService;
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

    //jpas for easy reset
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
    RequestJPA requestJPA;
    @Autowired
    BannedJPA bannedJPA;
    @Autowired
    AppointmentJPA appointmentJPA;
    @Autowired
    HourRequestJPA hourRequestJPA;
    @Autowired
    VolunteeringPostJPA volunteeringPostJPA;
    @Autowired
    ReportJPA reportJPA;

    private String username1, username2, username3, adminUsername;
    private String password1, password2, password3, adminPassword;
    private String name1, name2, name3, adminName;
    private String email1, email2, email3, adminEmail;
    private String phone1, phone2, phone3, adminPhone;
    private Date birthDate1, birthDate2, birthDate3, adminBirthDate;

    private String user1Token, user2Token, user3Token, adminToken;
    private int organizationId, volunteeringId;

    @BeforeEach
    void setUp() {
        messageJPA.deleteAll();
        volunteeringJPA.deleteAll();
        organizationJPA.deleteAll();
        notificationJPA.deleteAll();
        userJPA.deleteAll();
        volunteerPostJPA.deleteAll();
        requestJPA.deleteAll();
        bannedJPA.deleteAll();
        appointmentJPA.deleteAll();
        hourRequestJPA.deleteAll();
        reportJPA.deleteAll();
        volunteeringPostJPA.deleteAll();
        facadeManager.getAuthFacade().clearInvalidatedTokens();

        username1 = "user1";
        username2 = "user2";
        username3 = "user3";
        adminUsername = "admin";
        password1 = "ThisIsMyPassword1";
        password2 = "bad";
        password3 = null;
        adminPassword = "adminPassword";
        name1 = "User1";
        name2 = "User2";
        name3 = "User3";
        adminName = "Admin";
        email1 = "user1@dogood.com";
        email2 = "user2@dogood.com";
        email3 = "user3@dogood.com";
        adminEmail = "admin@dogood.com";
        phone1 = "0501234567";
        phone2 = "0501234568";
        phone3 = "0501234569";
        adminPhone = "0501234560";
        birthDate1 = new Date();
        birthDate2 = new Date(2000, Calendar.JANUARY,1);
        birthDate3 = null;
        adminBirthDate = new Date();

        facadeManager.getUsersFacade().registerAdmin(adminUsername,adminPassword,adminName,adminEmail,adminPhone,adminBirthDate);
        adminToken = userService.login(adminUsername,adminPassword).getData();

        Response<Integer> createOrganization = organizationService.createOrganization(adminToken,
                "Organization",
                "Description",
                "052-0520520",
                "organization@manager.com",
                adminUsername);
        organizationId = createOrganization.getData();
        Response<Integer> createVolunteering = organizationService.createVolunteering(adminToken,
                organizationId, "Volunteering", "Description", adminUsername);
        volunteeringId = createVolunteering.getData();

//        Response<String> login1 = userService.login(organizationMangerId, "123456");
//        Response<String> login2 = userService.login(aliceId, "123456");
//        Response<String> login3 = userService.login(bobId, "123456");
//        organizationManagerToken = login1.getData();
//        aliceToken = login2.getData();
//        bobToken = login3.getData();
    }

    // registration with automatic verification, with mock, for now.
    private void registerAndVerify(String username, String password, String name, String email, String phone, Date birthDate) {
        Response<String> response1 = userService.register(username,password,name,email,phone,birthDate,null);
        Assertions.assertFalse(response1.getError());
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(username),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail(email);
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        VerifyEmailRequest userEmailRequest = new VerifyEmailRequest(username,"");
        userService.verifyEmail(userEmailRequest);
    }

    //1.1
    @Test
    public void whenRegister_givenValidData_userCreated() {
        // registration with automatic verification for now.
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);

        Response<User> response2 = userService.getUserByUsername(username1);
        Assertions.assertFalse(response2.getError());
        Assertions.assertEquals(response2.getData().getUsername(),username1);
    }

    //1.1
    @Test
    public void whenRegister_givenInvalidData_userCreated() {
        // registration with automatic verification for now.
        // register with bad password
        Response<String> response1 = userService.register(username1,password2,name1,email1,phone1,birthDate1,null);
        Assertions.assertTrue(response1.getError());

        Response<User> response2 = userService.getUserByUsername(username1);
        Assertions.assertTrue(response2.getError());
    }

    //test register same username twice (1.1)
    @Test
    public void whenRegister_givenAlreadyRegistered_userNotCreated() {
        // registration with automatic verification for now.
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        Response<User> response1 = userService.getUserByUsername(username1);
        Assertions.assertFalse(response1.getError());
        Assertions.assertEquals(response1.getData().getUsername(),username1);

        Response<String> response2 = userService.register(username1,password1,name1,email1,phone1,birthDate1,null);
        Assertions.assertTrue(response2.getError());
    }

    // could also test email verification process during registration, simulate email sent process etc. (1.1)

    // test register with banned email (1.1, 5.2)
    @Test
    public void whenRegister_givenUsernameBanned_userNotCreated() {
        // ban user's email address
        reportService.banEmail(adminToken, adminUsername, email1);

        // attempt to register with banned email
        Response<String> response = userService.register(username1,password1,name1,email1,phone1,birthDate1,null);
        Assertions.assertTrue(response.getError());
    }

    // test email ban (5.2) - to be tested in reports service

    // test user ban (5.2)
    @Test
    public void whenUserBan_givenUserExists_userBanned() {
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token1 = userService.login(username1,password1).getData();

        // ban user1 from the system
        Response<Boolean> response1 = userService.banUser(adminToken,adminUsername,username1);
        Assertions.assertTrue(response1.getData()); // assert ban successful
        // attempt to make an action using the banned user, with old token
        Response<String> response2 = userService.updateUserFields(token1,username1,password1, List.of(email1,email2),name2,phone2);
        Assertions.assertTrue(response2.getError());
    }

    // test user ban (5.2)
    @Test
    public void whenUserBan_givenNotAdminUser_userNotBanned() {
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        registerAndVerify(username2,password1,name2,email2,phone2,birthDate1);
        String token1 = userService.login(username1,password1).getData();
        String token2 = userService.login(username2,password1).getData();

        // attempt to ban user2 from the system, using token1 (normal user token, not admin)
        Response<Boolean> response1 = userService.banUser(token1,username1,username2);
        Assertions.assertTrue(response1.getError()); // assert ban unsuccessful
    }

    // test user ban (5.2)
    @Test
    public void whenUserBan_givenUserDoesntExist_userNotBanned() {
        // attempt to ban non-existent user from the system
        Response<Boolean> response1 = userService.banUser(adminToken,adminUsername,username1);
        Assertions.assertTrue(response1.getError()); // assert ban unsuccessful
    }

    // test login (1.2)
    @Test
    public void whenLogin_givenUserExistsCorrectPassword_userLoggedIn() {
        // registration with automatic verification
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);

        Response<String> response = userService.login(username1,password1);
        Assertions.assertFalse(response.getError());
        Assertions.assertTrue(response.getData().length() > 0); // assert we receive some access token as a response value
    }

    // test login (1.2)
    @Test
    public void whenLogin_givenUserExistsIncorrectPassword_userNotLoggedIn() {
        // registration with automatic verification
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);

        Response<String> response = userService.login(username1,password2); // attempt log in with wrong password
        Assertions.assertTrue(response.getError());
    }

    // test login (1.2)
    @Test
    public void whenLogin_givenUserDoesntExist_userNotLoggedIn() {
        // registration with automatic verification
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);

        Response<String> response = userService.login(username2,password1); // attempt log in with wrong username
        Assertions.assertTrue(response.getError());
    }

    // test logout (1.3)
    @Test
    public void whenLogout_givenUserLoggedIn_userLogsOut() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        Response<String> response = userService.logout(token);
        Assertions.assertFalse(response.getError());
    }

    // test logout (1.3)
    @Test
    public void whenLogout_givenUserLoggedOut_error() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();
        userService.logout(token);

        Response<String> response = userService.logout(token); // attempt to log out again
        Assertions.assertTrue(response.getError());
    }

    // test update user info (1.4)
    @Test
    public void whenUpdateUserFields_givenValid_fieldsUpdated() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        String newPassword = "newPassword";
        String newPhone = phone2;

        Response<String> response = userService.updateUserFields(token,username1,newPassword,List.of(email1),name1,newPhone); // update password and phone
        Assertions.assertFalse(response.getError());
        User user = userService.getUserByToken(token).getData();
        Assertions.assertEquals(user.getPhone(),newPhone); // Assert phone updated
    }

    // test update user info (1.4)
    @Test
    public void whenUpdateUserFields_givenInvalid_error() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        String newPassword = "1";
        String newPhone = "00";

        Response<String> response = userService.updateUserFields(token,username1,newPassword,List.of(email1),name1,newPhone); // attempt to update password and phone to invalid values
        Assertions.assertTrue(response.getError());
        User user = userService.getUserByToken(token).getData();
        Assertions.assertEquals(user.getPhone(),phone1); // Assert phone not updated, still old
    }

    // test update user skills (2.1)
    @Test
    public void whenUpdateUserSkills_givenValid_skillsUpdated() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        List<String> newSkills = List.of("Programming","Animals");

        Response<String> response = userService.updateUserSkills(token,username1,newSkills); // update user skills
        Assertions.assertFalse(response.getError());
        User user = userService.getUserByToken(token).getData();
        Assertions.assertTrue(user.getSkills().containsAll(newSkills) && newSkills.containsAll(user.getSkills())); // Assert skills updated
    }

    // test update user skills (2.1)
    @Test
    public void whenUpdateUserSkills_givenInvalid_error() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        List<String> oldSkills = userService.getUserByToken(token).getData().getSkills();
        List<String> newSkills = null;

        Response<String> response = userService.updateUserSkills(token,username1,newSkills); // attempt to update skills to null list
        Assertions.assertTrue(response.getError());
        User user = userService.getUserByToken(token).getData();
        Assertions.assertTrue(user.getSkills().containsAll(oldSkills) && oldSkills.containsAll(user.getSkills())); // Assert skills not updated, still old
    }

    // test extract user skills and preferences from CV file (2.1, 2.2)
    @Test
    public void whenUploadCVGenerateSkillsAndPreferences_givenValid_skillsAndCategoriesUpdated() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        List<String> newSkills = List.of("Programming","Sports");
        List<String> newCategories = List.of("Animals","School");
        SkillsAndCategories resultObj = new SkillsAndCategories();
        resultObj.setSkills(newSkills);
        resultObj.setCategories(newCategories);
        ObjectMapper objectMapper = new ObjectMapper();

        byte[] cvBytes = {37,80,68,70,45,49,46,52,10,49,32,48,32,111,98,106,10,60,60,47,84,121,112,101,32,47,67,97,116,97,108,111,103,10,47,80,97,103,101,115,32,50,32,48,32,82,10,62,62,10,101,110,100,111,98,106,10,50,32,48,32,111,98,106,10,60,60,47,84,121,112,101,32,47,80,97,103,101,115,10,47,75,105,100,115,32,91,51,32,48,32,82,93,10,47,67,111,117,110,116,32,49,10,62,62,10,101,110,100,111,98,106,10,51,32,48,32,111,98,106,10,60,60,47,84,121,112,101,32,47,80,97,103,101,10,47,80,97,114,101,110,116,32,50,32,48,32,82,10,47,77,101,100,105,97,66,111,120,32,91,48,32,48,32,53,57,53,32,56,52,50,93,10,47,67,111,110,116,101,110,116,115,32,53,32,48,32,82,10,47,82,101,115,111,117,114,99,101,115,32,60,60,47,80,114,111,99,83,101,116,32,91,47,80,68,70,32,47,84,101,120,116,93,10,47,70,111,110,116,32,60,60,47,70,49,32,52,32,48,32,82,62,62,10,62,62,10,62,62,10,101,110,100,111,98,106,10,52,32,48,32,111,98,106,10,60,60,47,84,121,112,101,32,47,70,111,110,116,10,47,83,117,98,116,121,112,101,32,47,84,121,112,101,49,10,47,78,97,109,101,32,47,70,49,10,47,66,97,115,101,70,111,110,116,32,47,72,101,108,118,101,116,105,99,97,10,47,69,110,99,111,100,105,110,103,32,47,77,97,99,82,111,109,97,110,69,110,99,111,100,105,110,103,10,62,62,10,101,110,100,111,98,106,10,53,32,48,32,111,98,106,10,60,60,47,76,101,110,103,116,104,32,53,51,10,62,62,10,115,116,114,101,97,109,10,66,84,10,47,70,49,32,50,48,32,84,102,10,50,50,48,32,52,48,48,32,84,100,10,40,68,117,109,109,121,32,80,68,70,41,32,84,106,10,69,84,10,101,110,100,115,116,114,101,97,109,10,101,110,100,111,98,106,10,120,114,101,102,10,48,32,54,10,48,48,48,48,48,48,48,48,48,48,32,54,53,53,51,53,32,102,10,48,48,48,48,48,48,48,48,48,57,32,48,48,48,48,48,32,110,10,48,48,48,48,48,48,48,48,54,51,32,48,48,48,48,48,32,110,10,48,48,48,48,48,48,48,49,50,52,32,48,48,48,48,48,32,110,10,48,48,48,48,48,48,48,50,55,55,32,48,48,48,48,48,32,110,10,48,48,48,48,48,48,48,51,57,50,32,48,48,48,48,48,32,110,10,116,114,97,105,108,101,114,10,60,60,47,83,105,122,101,32,54,10,47,82,111,111,116,32,49,32,48,32,82,10,62,62,10,115,116,97,114,116,120,114,101,102,10,52,57,53,10,37,37,69,79,70,10}; // small pdf with text, so it won't crash
        MultipartFile cvPdf = new MockMultipartFile("mockFile",cvBytes);
        // Mock AI Answers to skills and categories from CV
        try {
            Mockito.when(gemini.sendQuery(anyString())).thenReturn(objectMapper.writeValueAsString(resultObj));
        } catch(Exception e) {Assertions.fail();}

        Response<Boolean> response = userService.uploadCV(token,username1,cvPdf); // update cv file
        Assertions.assertFalse(response.getError());
        Response<Boolean> response2 = userService.generateSkillsAndPreferences(token,username1);
        Assertions.assertFalse(response2.getError());

        User user = userService.getUserByToken(token).getData();
        Assertions.assertTrue(user.getSkills().containsAll(newSkills) && newSkills.containsAll(user.getSkills())); // Assert skills updated
        Assertions.assertTrue(user.getPreferredCategories().containsAll(newCategories) && newCategories.containsAll(user.getPreferredCategories())); // Assert categories updated
    }

    // test extract user skills and preferences from CV file (2.1, 2.2)
    @Test
    public void whenUploadCVGenerateSkillsAndPreferences_givenNullFile_error() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        Response<User> userResponse = userService.getUserByToken(token);
        System.out.println(userResponse.getError());
        List<String> oldSkills = userResponse.getData().getSkills();
        List<String> oldCategories = userResponse.getData().getPreferredCategories();
        MultipartFile cvPdf = null;

        userService.uploadCV(token,username1,cvPdf); // update cv to null file (removes it from the system)

        Response<Boolean> response = userService.generateSkillsAndPreferences(token,username1);
        Assertions.assertTrue(response.getError());

        User user = userService.getUserByToken(token).getData();
        Assertions.assertTrue(user.getSkills().containsAll(oldSkills) && oldSkills.containsAll(user.getSkills())); // Assert skills not updated, still old
        Assertions.assertTrue(user.getPreferredCategories().containsAll(oldCategories) && oldCategories.containsAll(user.getPreferredCategories())); // Assert categories not updated, still old
    }

    // test update user preferences (categories) (2.2)
    @Test
    public void whenUpdateUserPreferences_givenValid_skillsUpdated() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        List<String> newCategories = List.of("Animals","Healthcare");

        Response<String> response = userService.updateUserPreferences(token,username1,newCategories); // update user categories
        System.out.println(response.getError());
        Assertions.assertFalse(response.getError());
        User user = userService.getUserByToken(token).getData();
        Assertions.assertTrue(user.getPreferredCategories().containsAll(newCategories) && newCategories.containsAll(user.getPreferredCategories())); // Assert categories updated
    }

    // test update user preferences (categories) (2.2)
    @Test
    public void whenUpdateUserPreferences_givenInvalid_error() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();

        List<String> oldCategories = userService.getUserByToken(token).getData().getPreferredCategories();
        List<String> newCategories = null;

        Response<String> response = userService.updateUserPreferences(token,username1,newCategories); // attempt to update categories to null list
        Assertions.assertTrue(response.getError());
        User user = userService.getUserByToken(token).getData();
        Assertions.assertTrue(user.getPreferredCategories().containsAll(oldCategories) && oldCategories.containsAll(user.getPreferredCategories())); // Assert categories not updated, still old
    }

    // test show hours summary (2.10)
    // pretty much a duplication from volunteering acceptance test, cannot test filtering as it's only hidden in frontend side
    @Test
    public void whenGetApprovedHours_givenJoinedVolunteering_ValidHours() {
        // registration with automatic verification, then login
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        String token = userService.login(username1,password1).getData();
        volunteeringService.requestToJoinVolunteering(token,username1,volunteeringId,"test");
        volunteeringService.acceptUserJoinRequest(adminToken,adminUsername,volunteeringId,username1,0);

        // took from volunteering tests procedure:
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(14,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> requestHoursApproval = volunteeringService
                .requestHoursApproval(token,username1,volunteeringId,startTime,endTime);
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(adminToken,adminUsername,volunteeringId);
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        HourApprovalRequest request = hourApprovalRequests.get(0);
        Response<String> approveUserHours = volunteeringService
                .approveUserHours(adminToken,adminUsername,volunteeringId,username1,startTime,endTime);


        Response<List<HourApprovalRequest>> response = userService.getApprovedHours(token,username1); // Get the approved hours list
        Assertions.assertFalse(response.getError());
        List<HourApprovalRequest> hours = response.getData();
        Assertions.assertEquals(1,hours.size());
        HourApprovalRequest approved = hours.get(0);
        Assertions.assertTrue(approved.isApproved());
        Assertions.assertEquals(startTime.getTime(),approved.getStartTime().getTime());
        Assertions.assertEquals(endTime.getTime(),approved.getEndTime().getTime());
    }
}