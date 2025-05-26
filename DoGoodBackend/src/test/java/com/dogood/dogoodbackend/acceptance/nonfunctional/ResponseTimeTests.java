package com.dogood.dogoodbackend.acceptance.nonfunctional;

import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public class ResponseTimeTests {
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
    UseCases useCases;

    @BeforeEach
    public void setUp() {
        useCases.reset();
    }

    public void evaluateUseCaseDuration(UseCaseExecutor executor){
        long start = System.currentTimeMillis();
        Assertions.assertTrue(executor.execute());
        long end = System.currentTimeMillis();
        long duration = end - start;
        Assertions.assertTrue(duration <= 3000);
    }

    @Test
    public void registerResponseTimeTest(){
        evaluateUseCaseDuration(() -> useCases.registerUserUseCase("Username"));
    }

    @Test
    public void loginResponseTimeTest(){
        useCases.registerUserUseCase("Username");
        useCases.verifyUsername("Username");
        evaluateUseCaseDuration(() -> useCases.loginUserUseCase("Username") != null);
    }

    @Test
    public void logoutResponseTimeTest(){
        useCases.registerUserUseCase("Username");
        useCases.verifyUsername("Username");
        String token = useCases.loginUserUseCase("Username");
        evaluateUseCaseDuration(() -> useCases.logoutUserUseCase(token));
    }
    @Test
    public void createOrganizationResponseTimeTest(){
        useCases.registerUserUseCase("Username");
        useCases.verifyUsername("Username");
        String token = useCases.loginUserUseCase("Username");
        evaluateUseCaseDuration(() -> useCases.newOrganizationUseCase(token, "Username") > 0);
    }
}
