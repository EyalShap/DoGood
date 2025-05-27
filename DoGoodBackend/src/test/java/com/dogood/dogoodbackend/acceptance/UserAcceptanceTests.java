package com.dogood.dogoodbackend.acceptance;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.users.User;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

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

    private String username1, username2, username3;
    private String password1, password2, password3;
    private String name1, name2, name3;
    private String email1, email2, email3;
    private String phone1, phone2, phone3;
    private Date birthDate1, birthDate2, birthDate3;

    private String user1Token, user2Token, user3Token;

    @BeforeEach
    void setUp() {
        messageJPA.deleteAll();
        volunteeringJPA.deleteAll();
        organizationJPA.deleteAll();
        notificationJPA.deleteAll();
        userJPA.deleteAll();
        volunteerPostJPA.deleteAll();
        username1 = "user1";
        username2 = "user2";
        username3 = "user3";
        password1 = "ThisIsMyPassword1";
        password2 = "bad";
        password3 = null;
        name1 = "User1";
        name2 = "User2";
        name3 = "User3";
        email1 = "user1@dogood.com";
        email2 = "user2@dogood.com";
        email3 = "user3@dogood.com";
        phone1 = "0501234567";
        phone2 = "0501234568";
        phone3 = "0501234569";
        birthDate1 = new Date();
        birthDate2 = new Date(2000, Calendar.JANUARY,1);
        birthDate3 = null;

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
    public void whenRegister_givenValidData_userCreated(){
        // registration with automatic verification for now.
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);

        Response<User> response2 = userService.getUserByUsername(username1);
        Assertions.assertFalse(response2.getError());
        Assertions.assertEquals(response2.getData().getUsername(),username1);
    }

    //1.1
    @Test
    public void whenRegister_givenInvalidData_userCreated(){
        // registration with automatic verification for now.
        // register with bad password
        Response<String> response1 = userService.register(username1,password2,name1,email1,phone1,birthDate1,null);
        Assertions.assertTrue(response1.getError());

        Response<User> response2 = userService.getUserByUsername(username1);
        Assertions.assertTrue(response2.getError());
    }

    //1.1 - test register same username twice
    @Test
    public void whenRegister_givenAlreadyRegistered_userNotCreated(){
        // registration with automatic verification for now.
        // register with bad password
        registerAndVerify(username1,password1,name1,email1,phone1,birthDate1);
        Response<User> response1 = userService.getUserByUsername(username1);
        Assertions.assertFalse(response1.getError());
        Assertions.assertEquals(response1.getData().getUsername(),username1);

        Response<String> response2 = userService.register(username1,password1,name1,email1,phone1,birthDate1,null);
        Assertions.assertTrue(response2.getError());
    }

    // test verification? register with verification mock (1.1)
    // test register with banned email (1.1, 5.2)
    // test email ban (5.2)
    // test login (1.2)
    // test logout (1.3)
    // test update user info (1.4)
    // test update user skills (2.1)
    // test update user preferences (categories) (2.2)
}