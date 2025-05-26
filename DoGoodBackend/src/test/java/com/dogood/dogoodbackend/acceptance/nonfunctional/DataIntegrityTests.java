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

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@ActiveProfiles("test")
public class DataIntegrityTests {
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

    String[] usernames;
    private final int threadAmount = 10;

    @BeforeEach
    public void setUp() {
        useCases.reset();
        usernames = new String[threadAmount];
        for(int i = 0; i < usernames.length; i++){
            usernames[i] = "user" + i;
        }
    }


    @Test
    public void registerDataIntegrityTest() throws InterruptedException {
        boolean[] passOrFail = new boolean[threadAmount];
        ExecutorService executor = Executors.newFixedThreadPool(threadAmount);
        CountDownLatch latch = new CountDownLatch(threadAmount);

        for (int i = 0; i < threadAmount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    passOrFail[index] = useCases.registerUserUseCase(usernames[index]);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        int amountPass = 0;
        for(int i = 0; i < threadAmount; i++){
            amountPass += passOrFail[i] ? 1 : 0;
        }
        Assertions.assertEquals(threadAmount, amountPass);
    }

    @Test
    public void loginDataIntegrityTest() throws InterruptedException {
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
        }
        final boolean[] passOrFail = new boolean[threadAmount];
        ExecutorService executor = Executors.newFixedThreadPool(threadAmount);
        CountDownLatch latch = new CountDownLatch(threadAmount);

        for (int i = 0; i < threadAmount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    passOrFail[index] = useCases.loginUserUseCase(usernames[index]) != null;
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        int amountPass = 0;
        for(int i = 0; i < threadAmount; i++){
            amountPass += passOrFail[i] ? 1 : 0;
        }
        Assertions.assertEquals(threadAmount, amountPass);
    }

    @Test
    public void logoutDataIntegrityTest() throws InterruptedException {
        final String[] tokens = new String[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            tokens[i] = useCases.loginUserUseCase(usernames[i]);
        }
        final boolean[] passOrFail = new boolean[threadAmount];
        ExecutorService executor = Executors.newFixedThreadPool(threadAmount);
        CountDownLatch latch = new CountDownLatch(threadAmount);

        for (int i = 0; i < threadAmount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    passOrFail[index] = useCases.logoutUserUseCase(tokens[index]);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        int amountPass = 0;
        for(int i = 0; i < threadAmount; i++){
            amountPass += passOrFail[i] ? 1 : 0;
        }
        Assertions.assertEquals(threadAmount, amountPass);
    }

    @Test
    public void createOrganizationDataIntegrityTest() throws InterruptedException {
        final String[] tokens = new String[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            tokens[i] = useCases.loginUserUseCase(usernames[i]);
        }
        final boolean[] passOrFail = new boolean[threadAmount];
        final int[] ids = new int[threadAmount];
        ExecutorService executor = Executors.newFixedThreadPool(threadAmount);
        CountDownLatch latch = new CountDownLatch(threadAmount);

        for (int i = 0; i < threadAmount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    int id = useCases.newOrganizationUseCase(tokens[index], usernames[index]);
                    passOrFail[index] = id > 0;
                    ids[index] = id;
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        int amountPass = 0;
        Set<Integer> idSet = new HashSet<>();
        for(int i = 0; i < threadAmount; i++){
            amountPass += passOrFail[i] ? 1 : 0;
            idSet.add(ids[i]);
        }
        Assertions.assertEquals(threadAmount, amountPass);
        Assertions.assertEquals(threadAmount, idSet.size()); //all ids are unique
    }
}
