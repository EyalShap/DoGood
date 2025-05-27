package com.dogood.dogoodbackend.acceptance.nonfunctional;

import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itextpdf.text.DocumentException;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
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

    private boolean notNullAndGivenSize(Collection<?> collection, int size) {
        return collection != null && collection.size() == size;
    }

    private void evaluateDataIntegrity(ParametricUseCaseExecutor ucExecutor, Map<String,String>[] parameters, int requiredPass) throws InterruptedException {
        boolean[] passOrFail = new boolean[threadAmount];
        ExecutorService executor = Executors.newFixedThreadPool(threadAmount);
        CountDownLatch latch = new CountDownLatch(threadAmount);

        for (int i = 0; i < threadAmount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    passOrFail[index] = ucExecutor.execute(parameters[index]);
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
        Assertions.assertEquals(requiredPass, amountPass);
    }


    @Test
    public void registerDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            parameters[i] = new HashMap<>();
            parameters[i].put("username", usernames[i]);
        }
        evaluateDataIntegrity((parameters1 -> useCases.registerUserUseCase(parameters1.get("username"))),parameters,threadAmount);
    }

    @Test
    public void loginDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username", usernames[i]);
        }
        evaluateDataIntegrity((parameters1 -> useCases.loginUserUseCase(parameters1.get("username")) != null),parameters,threadAmount);

    }

    @Test
    public void logoutDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("token", token);
        }
        evaluateDataIntegrity((parameters1 -> useCases.logoutUserUseCase(parameters1.get("token"))),parameters,threadAmount);
    }

    @Test
    public void updateProfileDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        evaluateDataIntegrity((parameters1 -> useCases.updateUserDetailsUseCase(parameters1.get("token"),parameters1.get("username"))),parameters,threadAmount);
    }

    @Test
    public void updateSkillsDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        evaluateDataIntegrity((parameters1 -> useCases.updateUserSkillsUseCase(parameters1.get("token"),parameters1.get("username"))),parameters,threadAmount);
    }

    @Test
    public void updateCategoriesDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        evaluateDataIntegrity((parameters1 -> useCases.updateUserPreferencesUseCase(parameters1.get("token"),parameters1.get("username"))),parameters,threadAmount);
    }

    @Test
    public void filterPostsDataIntegrityTest() throws InterruptedException {
        useCases.registerUserUseCase("PostMaker");
        useCases.verifyUsername("PostMaker");
        String posterToken = useCases.loginUserUseCase("PostMaker");
        int orgId = useCases.newOrganizationUseCase(posterToken, "PostMaker");
        int volId1 = useCases.newVolunteeringUseCase(posterToken, "PostMaker",orgId);
        for(int i = 0; i < 50; i++){
            if(i == 34){
                Mockito.when(gemini.sendQuery(Mockito.anyString())).thenReturn("Keyword");
            }else{
                Mockito.when(gemini.sendQuery(Mockito.anyString())).thenReturn("");
            }
            useCases.newVolunteeringPostUseCase(posterToken, "PostMaker",volId1);
        }
        useCases.updateVolunteeringCategoriesUseCase(posterToken,"PostMaker",volId1);
        int volId2 = useCases.newVolunteeringUseCase(posterToken, "PostMaker",orgId);
        for(int i = 0; i < 50; i++){
            if(i == 34){
                Mockito.when(gemini.sendQuery(Mockito.anyString())).thenReturn("Keyword");
            }else{
                Mockito.when(gemini.sendQuery(Mockito.anyString())).thenReturn("");
            }
            useCases.newVolunteeringPostUseCase(posterToken, "PostMaker",volId2);
        }


        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        evaluateDataIntegrity((parameters1 -> notNullAndGivenSize(useCases.filterPostsUseCase(parameters1.get("token"),parameters1.get("username")),1)),parameters,threadAmount);
    }

    @Test
    public void createOrganizationDataIntegrityTest() throws InterruptedException {
        int[] ids = new int[threadAmount];
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
            parameters[i].put("index",""+i);
        }
        evaluateDataIntegrity((parameters1 -> {
            int orgId = useCases.newOrganizationUseCase(parameters1.get("token"),parameters1.get("username"));
            ids[Integer.parseInt(parameters1.get("index"))] = orgId;
            return orgId >= 0;

        }),parameters,threadAmount);
        Set<Integer> idSet = new HashSet<>();
        for(int i = 0; i < threadAmount; i++){
            idSet.add(ids[i]);
        }
        Assertions.assertEquals(threadAmount, idSet.size()); //all ids are unique
    }
    @Test
    public void createVolunteeringDataIntegrityTest() throws InterruptedException {
        int[] ids = new int[threadAmount];
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            useCases.sendOrganizationManagerRequestUseCase(manToken, "Manager",orgId,usernames[i]);
            useCases.handleOrganizationManagerRequestUseCase(token,usernames[i],orgId,true);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
            parameters[i].put("index",""+i);
        }
        evaluateDataIntegrity((parameters1 -> {
            int volId = useCases.newVolunteeringUseCase(parameters1.get("token"),parameters1.get("username"),orgId);
            ids[Integer.parseInt(parameters1.get("index"))] = volId;
            return volId >= 0;
        }),parameters,threadAmount);
        Set<Integer> idSet = new HashSet<>();
        for(int i = 0; i < threadAmount; i++){
            idSet.add(ids[i]);
        }
        Assertions.assertEquals(threadAmount, idSet.size());
        Assertions.assertEquals(threadAmount,useCases.getOrganizationVolunteeringAmount(manToken, "Manager",orgId));
    }

    @Test
    public void updateVolunteeringSkillsDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager",orgId);
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        evaluateDataIntegrity((parameters1 -> useCases.updateVolunteeringSkillsUseCase(parameters1.get("token"),parameters1.get("username"),volId))
                ,parameters,threadAmount);
    }

    @Test
    public void updateVolunteeringCategoriesDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            int orgId = useCases.newOrganizationUseCase(token, usernames[i]);
            int volId = useCases.newVolunteeringUseCase(token, usernames[i], orgId);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
            parameters[i].put("volId", ""+volId);
        }
        evaluateDataIntegrity((parameters1 -> useCases.updateVolunteeringCategoriesUseCase(parameters1.get("token"),parameters1.get("username"),Integer.parseInt(parameters1.get("volId"))))
                ,parameters,threadAmount);
    }

    @Test
    public void sendVolunteeringJoinRequestDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase( manToken, "Manager", orgId);
        evaluateDataIntegrity((parameters1 ->
                useCases.sendVolunteeringJoinRequestUseCase(
                        parameters1.get("token"),parameters1.get("username"),volId)),parameters,threadAmount);
        int requestAmount = 0;
        for(int i = 0; i < threadAmount; i++){
            requestAmount += useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,usernames[i]) ? 1 : 0;
        }
        Assertions.assertEquals(threadAmount, requestAmount);
    }

    @Test
    public void acceptVolunteeringJoinRequestDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase("Manager", manToken);
        int volId = useCases.newVolunteeringUseCase("Manager", manToken, orgId);
        for(int i = 0; i < threadAmount; i++){
            useCases.sendOrganizationManagerRequestUseCase(manToken,"Manager",orgId,usernames[i]);
            useCases.handleOrganizationManagerRequestUseCase(parameters[i].get("token"),usernames[i],orgId,true);
        }
        useCases.registerUserUseCase("Volunteer");
        useCases.verifyUsername("Volunteer");
        String volToken = useCases.loginUserUseCase("Volunteer");
        useCases.sendVolunteeringJoinRequestUseCase(volToken, "Volunteer",volId);
        evaluateDataIntegrity((parameters1 ->
                useCases.acceptVolunteeringJoinRequestUseCase(
                        parameters1.get("token"),parameters1.get("username"),volId,"Volunteer")),parameters,1);
    }

    @Test
    public void createLocationDataIntegrityTest() throws InterruptedException {
        int[] ids = new int[threadAmount];
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            int orgId = useCases.newOrganizationUseCase(token, usernames[i]);
            int volId = useCases.newVolunteeringUseCase(token, usernames[i], orgId);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
            parameters[i].put("volId", ""+volId);
            parameters[i].put("index",""+i);
        }
        evaluateDataIntegrity((parameters1 -> {
            int volId = useCases.newLocationUseCase(parameters1.get("token"),parameters1.get("username"),Integer.parseInt(parameters1.get("volId")));
            ids[Integer.parseInt(parameters1.get("index"))] = volId;
            return volId >= 0;
        }),parameters,threadAmount);
        Set<Integer> idSet = new HashSet<>();
        for(int i = 0; i < threadAmount; i++){
            idSet.add(ids[i]);
        }
        Assertions.assertEquals(threadAmount, idSet.size());
    }

}
