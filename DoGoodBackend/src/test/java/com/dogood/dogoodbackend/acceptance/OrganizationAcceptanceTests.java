package com.dogood.dogoodbackend.acceptance;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.organizations.OrganizationDTO;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.emailverification.VerificationData;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.*;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.dogood.dogoodbackend.utils.PostErrors;
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
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class OrganizationAcceptanceTests {
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
    private String mainOrgName,mainOrgDescription,mainOrgPhoneNumber,mainOrgEmail;

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
        adminBirthDate = new Date();

        facadeManager.getUsersFacade().registerAdmin(adminUsername,adminPassword,adminName,adminEmail,adminPhone,adminBirthDate);
        adminToken = userService.login(adminUsername,adminPassword).getData();
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        registerAndVerify(username2,password1,name2,email2,phone2,birthDate1);

        mainOrgName = "Organization";
        mainOrgDescription = "Description";
        mainOrgPhoneNumber = "052-0520520";
        mainOrgEmail = "organization@manager.com";

        Response<Integer> createOrganization = organizationService.createOrganization(adminToken,
                mainOrgName,
                mainOrgDescription,
                mainOrgPhoneNumber,
                mainOrgEmail,
                adminUsername);
        organizationId = createOrganization.getData();
        Response<Integer> createVolunteering = organizationService.createVolunteering(adminToken,
                organizationId, "Volunteering", "Description", adminUsername);
        volunteeringId = createVolunteering.getData();

        Response<String> login1 = userService.login(username1, password1);
        Response<String> login2 = userService.login(username2, password1);
        user1Token = login1.getData();
        user2Token = login2.getData();
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

    //1.5 - createOrganization
    @Test
    public void whenCreateOrganization_givenValidData_organizationCreated() {
        String orgName = "Org";
        String orgDescription = "Desc";
        String orgPhoneNumber = phone1;
        String orgEmail = email1;

        Response<Integer> response = organizationService.createOrganization(user1Token,orgName,orgDescription,orgPhoneNumber,orgEmail,username1);
        Assertions.assertFalse(response.getError());
        int orgId = response.getData();
        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,orgId,username1);
        Assertions.assertEquals(orgName,response2.getData().getName());
        Assertions.assertEquals(orgDescription,response2.getData().getDescription());
        Assertions.assertEquals(orgPhoneNumber,response2.getData().getPhoneNumber());
        Assertions.assertEquals(orgEmail,response2.getData().getEmail());
    }

    //1.5 - createOrganization
    @Test
    public void whenCreateOrganization_givenInvalidOrgName_organizationNotCreated() {
        String orgName = null;
        String orgDescription = "givenInvalidOrgName_organizationNotCreated";
        String orgPhoneNumber = phone1;
        String orgEmail = email1;

        Response<Integer> response = organizationService.createOrganization(user1Token,orgName,orgDescription,orgPhoneNumber,orgEmail,username1);
        Assertions.assertTrue(response.getError());
        Response<List<OrganizationDTO>> response2 = organizationService.getAllOrganizations(user1Token,username1);
        Assertions.assertFalse(response2.getError());
        for (OrganizationDTO org : response2.getData()) {
            Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
        }
    }

    //1.5 - createOrganization
    @Test
    public void whenCreateOrganization_givenInvalidOrgDescription_organizationNotCreated() {
        String orgName = "givenInvalidOrgDescription_organizationNotCreated";
        String orgDescription = null;
        String orgPhoneNumber = phone1;
        String orgEmail = email1;

        Response<Integer> response = organizationService.createOrganization(user1Token,orgName,orgDescription,orgPhoneNumber,orgEmail,username1);
        Assertions.assertTrue(response.getError());
        Response<List<OrganizationDTO>> response2 = organizationService.getAllOrganizations(user1Token,username1);
        Assertions.assertFalse(response2.getError());
        for (OrganizationDTO org : response2.getData()) {
            Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
        }
    }

    //1.5 - createOrganization
    @Test
    public void whenCreateOrganization_givenInvalidOrgPhoneNumber_organizationNotCreated() {
        String orgName = "Org";
        String orgDescription = "givenInvalidOrgPhoneNumber_organizationNotCreated";
        String orgPhoneNumber = null;
        String orgEmail = email1;

        Response<Integer> response = organizationService.createOrganization(user1Token,orgName,orgDescription,orgPhoneNumber,orgEmail,username1);
        Assertions.assertTrue(response.getError());
        Response<List<OrganizationDTO>> response2 = organizationService.getAllOrganizations(user1Token,username1);
        Assertions.assertFalse(response2.getError());
        for (OrganizationDTO org : response2.getData()) {
            Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
        }
    }

    //1.5 - createOrganization
    @Test
    public void whenCreateOrganization_givenInvalidOrgEmail_organizationNotCreated() {
        String orgName = "Org";
        String orgDescription = "givenInvalidOrgEmail_organizationNotCreated";
        String orgPhoneNumber = phone1;
        String orgEmail = null;

        Response<Integer> response = organizationService.createOrganization(user1Token,orgName,orgDescription,orgPhoneNumber,orgEmail,username1);
        Assertions.assertTrue(response.getError());
        Response<List<OrganizationDTO>> response2 = organizationService.getAllOrganizations(user1Token,username1);
        Assertions.assertFalse(response2.getError());
        for (OrganizationDTO org : response2.getData()) {
            Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
        }
    }

    //4.1 - editOrganization
    @Test
    public void whenEditOrganization_givenValidDetails_organizationUpdated() {
        String orgName = "Org";
        String orgDescription = "givenValidDetails_organizationUpdated";
        String orgPhoneNumber = phone1;
        String orgEmail = email1;

        Response<Boolean> response = organizationService.editOrganization(adminToken,organizationId,orgName,orgDescription,orgPhoneNumber,orgEmail,adminUsername);
        Assertions.assertFalse(response.getError());
        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertTrue(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
    }

    //4.1 - editOrganization
    @Test
    public void whenEditOrganization_givenValidDetailsDifferentManager_organizationUpdated() {
        String orgName = "Org";
        String orgDescription = "givenValidDetailsDifferentManager_organizationUpdated";
        String orgPhoneNumber = phone1;
        String orgEmail = email1;
        organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        organizationService.handleAssignManagerRequest(user1Token,username1,organizationId,true);

        Response<Boolean> response = organizationService.editOrganization(user1Token,organizationId,orgName,orgDescription,orgPhoneNumber,orgEmail,username1);
        Assertions.assertFalse(response.getError());
        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,organizationId,username1);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertTrue(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
    }

    //4.1 - editOrganization
    @Test
    public void whenEditOrganization_givenOrgDoesntExist_error() {
        String orgName = "Org";
        String orgDescription = "givenOrgDoesntExist_error";
        String orgPhoneNumber = phone1;
        String orgEmail = email1;
        int otherOrganizationId = -2;

        Response<Boolean> response = organizationService.editOrganization(adminToken,otherOrganizationId,orgName,orgDescription,orgPhoneNumber,orgEmail,adminUsername);
        Assertions.assertTrue(response.getError());
        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,otherOrganizationId,adminUsername);
        Assertions.assertTrue(response2.getError());
    }

    //4.1 - editOrganization
    @Test
    public void whenEditOrganization_givenInvalidOrgName_organizationNotUpdated() {
        String orgName = null;
        String orgDescription = "givenInvalidOrgName_organizationNotUpdated";
        String orgPhoneNumber = phone1;
        String orgEmail = email1;

        Response<Boolean> response = organizationService.editOrganization(adminToken,organizationId,orgName,orgDescription,orgPhoneNumber,orgEmail,adminUsername);
        Assertions.assertTrue(response.getError());
        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
    }

    //4.1 - editOrganization
    @Test
    public void whenEditOrganization_givenInvalidOrgDescription_organizationNotUpdated() {
        String orgName = "givenInvalidOrgDescription_organizationNotUpdated";
        String orgDescription = null;
        String orgPhoneNumber = phone1;
        String orgEmail = email1;

        Response<Boolean> response = organizationService.editOrganization(adminToken,organizationId,orgName,orgDescription,orgPhoneNumber,orgEmail,adminUsername);
        Assertions.assertTrue(response.getError());
        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
    }

    //4.1 - editOrganization
    @Test
    public void whenEditOrganization_givenInvalidOrgPhoneNumber_organizationNotUpdated() {
        String orgName = "Org";
        String orgDescription = "givenInvalidOrgPhoneNumber_organizationNotUpdated";
        String orgPhoneNumber = null;
        String orgEmail = email1;

        Response<Boolean> response = organizationService.editOrganization(adminToken,organizationId,orgName,orgDescription,orgPhoneNumber,orgEmail,adminUsername);
        Assertions.assertTrue(response.getError());
        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
    }

    //4.1 - editOrganization
    @Test
    public void whenEditOrganization_givenInvalidOrgEmail_organizationNotUpdated() {
        String orgName = "Org";
        String orgDescription = "givenInvalidOrgEmail_organizationNotUpdated";
        String orgPhoneNumber = phone1;
        String orgEmail = null;

        Response<Boolean> response = organizationService.editOrganization(adminToken,organizationId,orgName,orgDescription,orgPhoneNumber,orgEmail,adminUsername);
        Assertions.assertTrue(response.getError());
        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
    }

    //4.1 - editOrganization
    @Test
    public void whenEditOrganization_givenNotManager_organizationNotUpdated() {
        String orgName = "Org";
        String orgDescription = "givenNotManager_organizationNotUpdated";
        String orgPhoneNumber = phone1;
        String orgEmail = email1;

        Response<Boolean> response = organizationService.editOrganization(user1Token,organizationId,orgName,orgDescription,orgPhoneNumber,orgEmail,username1);
        Assertions.assertTrue(response.getError());
        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,organizationId,username1);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertFalse(org.getDescription().equals(orgDescription) && org.getPhoneNumber().equals(orgPhoneNumber) && org.getEmail().equals(orgEmail));
    }

    // 5.4 - removeOrganization
    @Test
    public void whenRemoveOrganization_givenFounder_organizationRemoved() {
        Response<Boolean> response = organizationService.removeOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertFalse(response.getError()); // founder deletes his organization
        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertTrue(response2.getError()); // assert organization is removed
    }

    // 5.4 - removeOrganization
    @Test
    public void whenRemoveOrganization_givenOrgDoesntExist_error() {
        int otherOrganizationId = -2;
        Response<OrganizationDTO> response0 = organizationService.getOrganization(adminToken,otherOrganizationId,adminUsername);
        Assertions.assertTrue(response0.getError()); // ensure organization doesn't exist

        Response<Boolean> response = organizationService.removeOrganization(adminToken,otherOrganizationId,adminUsername);
        Assertions.assertTrue(response.getError()); // founder attempts to delete a non-existent organization
    }

    // 5.4 - removeOrganization
    @Test
    public void whenRemoveOrganization_givenNotFounder_organizationNotRemoved() {
        // setup - make another manager, who is not a founder
        organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        organizationService.handleAssignManagerRequest(user1Token,username1,organizationId,true);

        Response<Boolean> response = organizationService.removeOrganization(user1Token,organizationId,username1);
        Assertions.assertTrue(response.getError()); // non-founder attempts to delete a non-existent organization
        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,organizationId,username1);
        Assertions.assertFalse(response2.getError()); // ensure organization still exists
        OrganizationDTO org = response2.getData();
        Assertions.assertTrue(org.getName().equals(mainOrgName) && org.getDescription().equals(mainOrgDescription) && org.getPhoneNumber().equals(mainOrgPhoneNumber) && org.getEmail().equals(mainOrgEmail));
    }

    // 4.13 - uploadSignature
    @Test
    public void whenUploadSignature_givenFounder_signatureUploaded() {
        // setup - get signature file
        MockMultipartFile signatureFile = null;
        try {
            File signature = ResourceUtils.getFile("classpath:signature-example.png");
            FileInputStream signatureStream = new FileInputStream(signature);
            signatureFile = new MockMultipartFile("signature-example.png", signatureStream);
        } catch (Exception e) { Assertions.fail();}

        Response<Boolean> response = organizationService.uploadSignature(adminToken,organizationId,adminUsername,signatureFile);
        Assertions.assertFalse(response.getError()); // founder attempts to upload a signature to his organization
        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertFalse(response2.getError());
        try {
            Assertions.assertArrayEquals(signatureFile.getBytes(), response2.getData().getSignature()); // assert signature has been uploaded
        } catch (Exception e) { Assertions.fail(); }
    }

    // 4.13 - uploadSignature
    @Test
    public void whenUploadSignature_givenNotFounder_signatureNotUploaded() {
        // setup - get signature file
        MockMultipartFile signatureFile = null;
        try {
            File signature = ResourceUtils.getFile("classpath:signature-example.png");
            FileInputStream signatureStream = new FileInputStream(signature);
            signatureFile = new MockMultipartFile("signature-example.png", signatureStream);
        } catch (Exception e) { Assertions.fail();}

        Response<Boolean> response = organizationService.uploadSignature(user1Token,organizationId,username1,signatureFile);
        Assertions.assertTrue(response.getError()); // founder attempts to upload a signature to his organization
        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,organizationId,username1);
        Assertions.assertFalse(response2.getError());
        try {
            Assertions.assertFalse(Arrays.equals(signatureFile.getBytes(),response2.getData().getSignature())); // assert signature has been uploaded
        } catch (Exception e) { Assertions.fail(); }
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenSendAssignManagerRequest_givenValid_requestSent() {
        Response<Boolean> response = organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        Assertions.assertFalse(response.getError());
        Response<List<Request>> response2 = organizationService.getUserRequests(user1Token,username1);
        Assertions.assertFalse(response2.getError());
        Request request = response2.getData().get(0);
        Assertions.assertTrue(request.getRequestObject() == RequestObject.ORGANIZATION
                && request.getAssignerUsername().equals(adminUsername)
                && request.getAssigneeUsername().equals(username1)
        );
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenSendAssignManagerRequest_givenByOtherManager_requestSent() {
        organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        organizationService.handleAssignManagerRequest(user1Token,username1,organizationId,true);

        Response<Boolean> response = organizationService.sendAssignManagerRequest(user1Token,username2,username1,organizationId);
        Assertions.assertFalse(response.getError());
        Response<List<Request>> response2 = organizationService.getUserRequests(user2Token,username2);
        Assertions.assertFalse(response2.getError());
        Request request = response2.getData().get(0);
        Assertions.assertTrue(request.getRequestObject() == RequestObject.ORGANIZATION
                && request.getAssignerUsername().equals(username1)
                && request.getAssigneeUsername().equals(username2)
        );
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenSendAssignManagerRequest_givenManagerDoesntExist_error() {
        String managerUsername = "NonExistentUser";

        Response<Boolean> response = organizationService.sendAssignManagerRequest(adminToken,managerUsername,adminUsername,organizationId);
        Assertions.assertTrue(response.getError());
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenSendAssignManagerRequest_givenNotManager_requestNotSent() {
        Response<Boolean> response = organizationService.sendAssignManagerRequest(user1Token,username2,username1,organizationId);
        Assertions.assertTrue(response.getError()); // non-manager cannot send manager request
        Response<List<Request>> response2 = organizationService.getUserRequests(user2Token,username2);
        Assertions.assertFalse(response2.getError());
        Assertions.assertEquals(0,response2.getData().size()); // ensure user2 didn't get the manager request
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenSendAssignManagerRequest_givenAlreadySent_requestNotSent() {
        Response<Boolean> response = organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        Assertions.assertFalse(response.getError());

        Response<Boolean> response2 = organizationService.sendAssignManagerRequest(adminUsername,username1,adminUsername,organizationId); // send manager request again
        Assertions.assertTrue(response2.getError()); // assert second request fails

        Response<List<Request>> response3 = organizationService.getUserRequests(user1Token,username1);
        Assertions.assertFalse(response3.getError());
        Assertions.assertEquals(1, response3.getData().size());
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenSendAssignManagerRequest_givenAlreadyManager_requestNotSent() {
        organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        organizationService.handleAssignManagerRequest(user1Token,username1,organizationId,true);

        Response<Boolean> response2 = organizationService.sendAssignManagerRequest(adminUsername,username1,adminUsername,organizationId); // send manager request again
        Assertions.assertTrue(response2.getError()); // assert second request fails

        Response<List<Request>> response3 = organizationService.getUserRequests(user1Token,username1);
        Assertions.assertFalse(response3.getError());
        Assertions.assertEquals(0, response3.getData().size()); // first request is deleted for some reason, after it's accepted, so ensure there are 0 requests now
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenHandleAssignManagerRequest_givenAccepting_becomesManager() {
        // setup - send manager request
        organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        boolean approved = true;

        Response<Boolean> response = organizationService.handleAssignManagerRequest(user1Token,username1,organizationId,approved);
        Assertions.assertFalse(response.getError());

        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,organizationId,username1);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertTrue(org.getManagerUsernames().contains(username1)); // assert user1 is now a manager
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenHandleAssignManagerRequest_givenRejecting_becomesManager() {
        // setup - send manager request
        organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        boolean approved = false;

        Response<Boolean> response = organizationService.handleAssignManagerRequest(user1Token,username1,organizationId,approved);
        Assertions.assertFalse(response.getError());

        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,organizationId,username1);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertFalse(org.getManagerUsernames().contains(username1)); // assert user1 is not a manager now
    }

    // 4.15 - sendAssignManagerRequest, handleAssignManagerRequest
    @Test
    public void whenHandleAssignManagerRequest_givenRequestDoesntExist_error() {
        boolean approved = false;

        Response<Boolean> response = organizationService.handleAssignManagerRequest(user1Token,username1,organizationId,approved);
        Assertions.assertTrue(response.getError()); // assert user1 cannot accept a non-existent request

        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,organizationId,username1);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertFalse(org.getManagerUsernames().contains(username1)); // assert user1 is not a manager now
    }

    // 4.2 - createVolunteering
    @Test
    public void whenCreateVolunteering_givenValidDetails_volunteeringCreated() {
        String volunteeringName = "Volunteering";
        String volunteeringDescription = "givenValidDetails_volunteeringCreated";
        Response<Integer> response = organizationService.createVolunteering(adminToken,organizationId,volunteeringName,volunteeringDescription,adminUsername);
        Assertions.assertFalse(response.getError()); // assert volunteering is created
        int newVolunteeringId = response.getData();

        Response<OrganizationDTO> response2 = organizationService.getOrganization(adminToken,organizationId,adminUsername);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertTrue(org.getVolunteeringIds().contains(newVolunteeringId)); // assert volunteering is created properly under the same organization

        Response<List<VolunteeringDTO>> response3 = organizationService.getOrganizationVolunteerings(adminToken,adminUsername,organizationId);
        Assertions.assertFalse(response3.getError());
        boolean found = false;
        for (VolunteeringDTO volunteering : response3.getData()) {
            if (volunteering.getId() == newVolunteeringId) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found); // assert again that the volunteering is created properly, under the same organization
    }

    // 4.2 - createVolunteering
    @Test
    public void whenCreateVolunteering_givenByOtherManager_volunteeringCreated() {
        organizationService.sendAssignManagerRequest(adminToken,username1,adminUsername,organizationId);
        organizationService.handleAssignManagerRequest(user1Token,username1,organizationId,true);

        String volunteeringName = "Volunteering";
        String volunteeringDescription = "givenByOtherManager_volunteeringCreated";
        Response<Integer> response = organizationService.createVolunteering(user1Token,organizationId,volunteeringName,volunteeringDescription,username1);
        Assertions.assertFalse(response.getError()); // assert volunteering is created, by other manager
        int newVolunteeringId = response.getData();

        Response<OrganizationDTO> response2 = organizationService.getOrganization(user1Token,organizationId,username1);
        Assertions.assertFalse(response2.getError());
        OrganizationDTO org = response2.getData();
        Assertions.assertTrue(org.getVolunteeringIds().contains(newVolunteeringId)); // assert volunteering is created properly under the same organization

        Response<List<VolunteeringDTO>> response3 = organizationService.getOrganizationVolunteerings(user1Token,username1,organizationId);
        Assertions.assertFalse(response3.getError());
        boolean found = false;
        for (VolunteeringDTO volunteering : response3.getData()) {
            if (volunteering.getId() == newVolunteeringId) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found); // assert again that the volunteering is created properly, under the same organization
    }

    // 4.2 - createVolunteering
    @Test
    public void whenCreateVolunteering_givenNonExistentOrg_volunteeringNotCreated() {
        String volunteeringName = "Volunteering";
        String volunteeringDescription = "givenNonExistentOrg_volunteeringNotCreated";
        int otherOrganizationId = -2; // non-existent organization

        Response<Integer> response = organizationService.createVolunteering(adminToken,otherOrganizationId,volunteeringName,volunteeringDescription,adminUsername);
        Assertions.assertTrue(response.getError()); // assert volunteering is not created
    }

    // 4.2 - createVolunteering
    @Test
    public void whenCreateVolunteering_givenInvalidVolunteeringName_volunteeringNotCreated() {
        String volunteeringName = null;
        String volunteeringDescription = "givenInvalidVolunteeringName_volunteeringNotCreated";

        Response<Integer> response = organizationService.createVolunteering(adminToken,organizationId,volunteeringName,volunteeringDescription,adminUsername);
        Assertions.assertTrue(response.getError()); // assert volunteering is not created
    }

    // 4.2 - createVolunteering
    @Test
    public void whenCreateVolunteering_givenInvalidVolunteeringDescription_volunteeringNotCreated() {
        String volunteeringName = "givenInvalidVolunteeringDescription_volunteeringNotCreated";
        String volunteeringDescription = null;

        Response<Integer> response = organizationService.createVolunteering(adminToken,organizationId,volunteeringName,volunteeringDescription,adminUsername);
        Assertions.assertTrue(response.getError()); // assert volunteering is not created
    }

    // 4.2 - createVolunteering
    @Test
    public void whenCreateVolunteering_givenNotByManager_volunteeringNotCreated() {
        String volunteeringName = "Volunteering";
        String volunteeringDescription = "givenNotByManager_volunteeringNotCreated";

        Response<Integer> response = organizationService.createVolunteering(user1Token,organizationId,volunteeringName,volunteeringDescription,username1);
        Assertions.assertTrue(response.getError()); // assert volunteering is not created
    }
}