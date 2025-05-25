package com.dogood.dogoodbackend.acceptance.nonfunctional;

import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.*;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;

@Service
public class UseCases {
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

    @Autowired
    FacadeManager facadeManager;
    @Autowired
    private ReportService reportService;

    public boolean registerUserUseCase(String username){
        Response<String> response = userService.register(username, "123456", "Name name", "email@gmail.com", "052-0520520", new Date(), "");
        return !response.getError();
    }

    public void verifyUsername(String username){

    }

    public boolean loginUserUseCase(String username){
        Response<String> response = userService.login(username, "123456");
        return !response.getError();
    }

    public boolean blockEmailUseCase(String username, String emailToBlock){
        facadeManager.getUsersFacade().registerAdmin(username, "admin123", "Admin adm", "admin@admin.com", "052-0520520", new Date());
        Response<String> loginResponse = userService.login(username, "admin123");
        String adminToken = loginResponse.getData();
        Response<Boolean> banEmail = reportService.banEmail(adminToken, username, emailToBlock);
        Response<String> register = userService.register("Username", "123456", "Name name", emailToBlock, "052-0520520", new Date(), "");
        return !banEmail.getError() && register.getError();
    }
}
