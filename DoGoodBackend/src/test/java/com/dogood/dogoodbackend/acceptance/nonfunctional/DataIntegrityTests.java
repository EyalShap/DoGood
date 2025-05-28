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
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

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
    AsyncUseCases asyncUseCases;

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
        Future<Boolean>[] futures = new Future[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            futures[i] = ucExecutor.execute(parameters[i]);
        }

        int passed = 0;
        for (Future<Boolean> future : futures) {
            try {
                if (future.get()) { // blocks until each result is ready
                    passed++;
                }
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        Assertions.assertEquals(requiredPass, passed);
    }


    @RepeatedTest(20)
    public void registerDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        for(int i = 0; i < threadAmount; i++){
            parameters[i] = new HashMap<>();
            parameters[i].put("username", "Username");
        }
        evaluateDataIntegrity((parameters1 -> asyncUseCases.registerUserUseCase(parameters1.get("username"))),parameters,1);
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
        evaluateDataIntegrity((parameters1 -> asyncUseCases.loginUserUseCase(parameters1.get("username")).thenApply(token -> token != null)),parameters,threadAmount);

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
        evaluateDataIntegrity((parameters1 -> asyncUseCases.logoutUserUseCase(parameters1.get("token"))),parameters,threadAmount);
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
        evaluateDataIntegrity((parameters1 -> asyncUseCases.updateUserDetailsUseCase(parameters1.get("token"),parameters1.get("username"))),parameters,threadAmount);
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
        evaluateDataIntegrity((parameters1 -> asyncUseCases.updateUserSkillsUseCase(parameters1.get("token"),parameters1.get("username"))),parameters,threadAmount);
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
        evaluateDataIntegrity((parameters1 -> asyncUseCases.updateUserPreferencesUseCase(parameters1.get("token"),parameters1.get("username"))),parameters,threadAmount);
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
        evaluateDataIntegrity((parameters1 -> asyncUseCases.filterPostsUseCase(parameters1.get("token"),parameters1.get("username")).thenApply(list -> notNullAndGivenSize(list,1))),parameters,threadAmount);
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
        evaluateDataIntegrity((parameters1 ->
                asyncUseCases.newOrganizationUseCase(parameters1.get("token"),parameters1.get("username")).thenApply(orgId -> {
                    ids[Integer.parseInt(parameters1.get("index"))] = orgId;
                    return orgId >= 0;
                })
        ),parameters,threadAmount);
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
        evaluateDataIntegrity((parameters1 -> asyncUseCases.newVolunteeringUseCase(parameters1.get("token"),parameters1.get("username"),orgId)
                .thenApply(volId -> {
                    ids[Integer.parseInt(parameters1.get("index"))] = volId;
                    return volId >= 0;
                })),parameters,threadAmount);
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
            useCases.sendOrganizationManagerRequestUseCase(manToken, "Manager",orgId,usernames[i]);
            useCases.handleOrganizationManagerRequestUseCase(token,usernames[i],orgId,true);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        evaluateDataIntegrity((parameters1 -> asyncUseCases.updateVolunteeringSkillsUseCase(parameters1.get("token"),parameters1.get("username"),volId))
                ,parameters,threadAmount);
    }

    @Test
    public void updateVolunteeringCategoriesDataIntegrityTest() throws InterruptedException {
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
            useCases.sendOrganizationManagerRequestUseCase(manToken, "Manager",orgId,usernames[i]);
            useCases.handleOrganizationManagerRequestUseCase(token,usernames[i],orgId,true);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
        }
        evaluateDataIntegrity((parameters1 -> asyncUseCases.updateVolunteeringCategoriesUseCase(parameters1.get("token"),parameters1.get("username"),volId))
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
                asyncUseCases.sendVolunteeringJoinRequestUseCase(
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
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        for(int i = 0; i < threadAmount; i++){
            useCases.sendOrganizationManagerRequestUseCase(manToken,"Manager",orgId,usernames[i]);
            useCases.handleOrganizationManagerRequestUseCase(parameters[i].get("token"),usernames[i],orgId,true);
        }
        useCases.registerUserUseCase("Volunteer");
        useCases.verifyUsername("Volunteer");
        String volToken = useCases.loginUserUseCase("Volunteer");
        useCases.sendVolunteeringJoinRequestUseCase(volToken, "Volunteer",volId);
        evaluateDataIntegrity((parameters1 ->
                asyncUseCases.acceptVolunteeringJoinRequestUseCase(
                        parameters1.get("token"),parameters1.get("username"),volId,"Volunteer")),parameters,1);
    }

    @Test
    public void createLocationDataIntegrityTest() throws InterruptedException {
        int[] ids = new int[threadAmount];
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
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
        evaluateDataIntegrity(parameters1 ->
                asyncUseCases.newLocationUseCase(parameters1.get("token"),parameters1.get("username"),volId).thenApply(
                        locId ->{
                            ids[Integer.parseInt(parameters1.get("index"))] = locId;
                            return locId >= 0;
                        }
                ),parameters,threadAmount);
        Set<Integer> idSet = new HashSet<>();
        for(int i = 0; i < threadAmount; i++){
            idSet.add(ids[i]);
        }
        Assertions.assertEquals(threadAmount, idSet.size());
        Assertions.assertEquals(threadAmount, useCases.getVolunteeringLocationAmount(manToken, "Manager",volId));
    }

    @Test
    public void createRangeDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken,"Manager",volId);
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
        evaluateDataIntegrity(parameters1 ->
                asyncUseCases.newRangeUseCase(parameters1.get("token"),parameters1.get("username"),volId,locId).thenApply(
                        rangeId -> rangeId >= 0),parameters,1);
        Assertions.assertEquals(1, useCases.getVolunteeringRangeAmount(manToken, "Manager",volId,locId));
    }

    @Test
    public void chooseLocationDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken, "Manager", volId);
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            useCases.sendVolunteeringJoinRequestUseCase(token, usernames[i],volId);
            useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
            parameters[i].put("index",""+i);
        }
        evaluateDataIntegrity(parameters1 ->
                asyncUseCases.chooseVolunteeringLocationUseCase(parameters1.get("token"),parameters1.get("username"),volId,locId),parameters,threadAmount);
    }

    @Test
    public void chooseRangeDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken, "Manager", volId);
        int rangeId = useCases.newRangeUseCase(manToken, "Manager", volId,locId);
        useCases.restrictRangeUseCase(manToken, "Manager",volId,locId,rangeId);
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            useCases.sendVolunteeringJoinRequestUseCase(token, usernames[i],volId);
            useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
            parameters[i].put("index",""+i);
        }

        evaluateDataIntegrity(parameters1 ->
                asyncUseCases.chooseVolunteeringRangeUseCase(parameters1.get("token"),parameters1.get("username"),volId,rangeId,locId),parameters,2);
    }

    @Test
    public void sendVolunteeringChatMessageDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            useCases.sendVolunteeringJoinRequestUseCase(token, usernames[i],volId);
            useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,usernames[i]);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
            parameters[i].put("index",""+i);
        }
        evaluateDataIntegrity(parameters1 ->
                asyncUseCases.sendVolunteeringChatMessageUseCase(parameters1.get("token"),parameters1.get("username"),volId),parameters,10);
        Assertions.assertEquals(threadAmount, useCases.getVolunteeringChatMessageAmount(manToken, "Manager",volId));
    }

    @Test
    public void scanCodeDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken, "Manager", volId);
        int rangeId = useCases.newRangeUseCase(manToken, "Manager", volId,locId);
        useCases.chooseScanTypeUseCase(manToken, "Manager",volId);
        for(int i = 0; i < threadAmount; i++){
            useCases.registerUserUseCase(usernames[i]);
            useCases.verifyUsername(usernames[i]);
            String token = useCases.loginUserUseCase(usernames[i]);
            useCases.sendVolunteeringJoinRequestUseCase(token, usernames[i],volId);
            useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,usernames[i]);
            useCases.chooseVolunteeringRangeUseCase(token, usernames[i],volId,rangeId,locId);
            parameters[i] = new HashMap<>();
            parameters[i].put("username",usernames[i]);
            parameters[i].put("token", token);
            parameters[i].put("index",""+i);
        }
        String code = useCases.createConstantQrCodeUseCase(manToken, "Manager",volId);
        evaluateDataIntegrity(parameters1 ->
                asyncUseCases.scanCodeUseCase(parameters1.get("token"),parameters1.get("username"),code),parameters,10);
        for(int i = 0; i < threadAmount; i++){
            Assertions.assertEquals(1,
                    useCases.viewSummaryUseCase(parameters[i].get("token"),parameters[i].get("username")).size());
        }
    }

    @Test
    public void manualHourApprovalDataIntegrityTest() throws InterruptedException {
        Map<String,String>[] parameters = new Map[threadAmount];
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken,"Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken,"Manager",volId);
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
        useCases.registerUserUseCase("Volunteer");
        useCases.verifyUsername("Volunteer");
        String volToken = useCases.loginUserUseCase("Volunteer");
        useCases.sendVolunteeringJoinRequestUseCase(volToken, "Volunteer", volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken, "Manager",volId,"Volunteer");
        useCases.makeHourRequestUseCase(volToken, "Volunteer", volId);
        evaluateDataIntegrity(parameters1 ->
                asyncUseCases.approveHoursUseCase(parameters1.get("token"), parameters1.get("username"),volId,"Volunteer"),parameters,1);
        Assertions.assertEquals(1, useCases.viewSummaryUseCase(volToken, "Volunteer").size());
    }
}
