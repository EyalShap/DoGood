package com.dogood.dogoodbackend.acceptance.nonfunctional;

import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.Collection;

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

    final String USERNAME = "Username";

    @Autowired
    UseCases useCases;

    @BeforeEach
    public void setUp() {
        useCases.reset();
    }

    private boolean notNullAndGivenSize(Collection<?> collection, int size) {
        return collection != null && collection.size() == size;
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
        evaluateUseCaseDuration(() -> useCases.registerUserUseCase(USERNAME));
    }

    @Test
    public void loginResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        evaluateUseCaseDuration(() -> useCases.loginUserUseCase(USERNAME) != null);
    }

    @Test
    public void logoutResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        evaluateUseCaseDuration(() -> useCases.logoutUserUseCase(token));
    }

    @Test
    public void updateProfileResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        evaluateUseCaseDuration(() -> useCases.updateUserDetailsUseCase(token,USERNAME));
    }

    @Test
    public void updateSkillsResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        evaluateUseCaseDuration(() -> useCases.updateUserSkillsUseCase(token,USERNAME));
    }

    @Test
    public void updateCategoriesResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        evaluateUseCaseDuration(() -> useCases.updateUserPreferencesUseCase(token,USERNAME));
    }

    @Test
    public void filterPostsResponseTimeTest(){
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


        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        evaluateUseCaseDuration(() -> notNullAndGivenSize(useCases.filterPostsUseCase(token,USERNAME),1));
    }
    
    @Test
    public void createOrganizationResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        evaluateUseCaseDuration(() -> useCases.newOrganizationUseCase(token, USERNAME) >= 0);
    }

    @Test
    public void createVolunteeringResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        evaluateUseCaseDuration(() -> useCases.newVolunteeringUseCase(token, USERNAME,orgId) >= 0);
    }

    @Test
    public void updateVolunteeringSkillsResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        int volId= useCases.newVolunteeringUseCase(token, USERNAME, orgId);
        evaluateUseCaseDuration(() -> useCases.updateVolunteeringSkillsUseCase(token,USERNAME,volId));
    }

    @Test
    public void updateVolunteeringCategoriesResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        int volId= useCases.newVolunteeringUseCase(token, USERNAME, orgId);
        evaluateUseCaseDuration(() -> useCases.updateVolunteeringCategoriesUseCase(token,USERNAME,volId));
    }

    @Test
    public void sendVolunteeringJoinRequestResponseTimeTest(){
        useCases.registerUserUseCase("VolMaker");
        useCases.verifyUsername("VolMaker");
        String posterToken = useCases.loginUserUseCase("VolMaker");
        int orgId = useCases.newOrganizationUseCase(posterToken, "VolMaker");
        int volId = useCases.newVolunteeringUseCase(posterToken, "VolMaker",orgId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        evaluateUseCaseDuration(() -> useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId));
    }

    @Test
    public void acceptVolunteeringJoinRequestResponseTimeTest(){
        useCases.registerUserUseCase("Volunteer");
        useCases.verifyUsername("Volunteer");
        String volToken = useCases.loginUserUseCase("Volunteer");

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        int volId= useCases.newVolunteeringUseCase(token, USERNAME, orgId);
        useCases.sendVolunteeringJoinRequestUseCase(volToken, "Volunteer",volId);
        evaluateUseCaseDuration(() -> useCases.acceptVolunteeringJoinRequestUseCase(token,USERNAME,volId,"Volunteer"));
    }

    @Test
    public void createLocationResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        int volId= useCases.newVolunteeringUseCase(token, USERNAME, orgId);
        evaluateUseCaseDuration(() -> useCases.newLocationUseCase(token,USERNAME,volId) >= 0);
    }

    @Test
    public void createRangeResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        int volId= useCases.newVolunteeringUseCase(token, USERNAME, orgId);
        int locId = useCases.newLocationUseCase(token,USERNAME,volId);
        evaluateUseCaseDuration(() -> useCases.newRangeUseCase(token,USERNAME,volId,locId) >= 0);
    }

    @Test
    public void chooseLocationResponseTimeTest(){
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken,"Manager",volId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);

        evaluateUseCaseDuration(() -> useCases.chooseVolunteeringLocationUseCase(token,USERNAME,volId,locId));
    }

    @Test
    public void chooseRangeResponseTimeTest(){
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken,"Manager",volId);
        int rangeId = useCases.newRangeUseCase(manToken,"Manager",volId,locId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);

        evaluateUseCaseDuration(() -> useCases.chooseVolunteeringRangeUseCase(token,USERNAME,volId,rangeId,locId));
    }

    @Test
    public void sendVolunteeringChatMessageResponseTimeTest(){
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);

        evaluateUseCaseDuration(() -> useCases.sendVolunteeringChatMessageUseCase(token, USERNAME, volId));
    }

    @Test
    public void scanCodeResponseTimeTest(){
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken,"Manager",volId);
        int rangeId = useCases.newRangeUseCase(manToken,"Manager",volId,locId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);
        useCases.chooseVolunteeringRangeUseCase(token,USERNAME,volId,rangeId,locId);
        evaluateUseCaseDuration(() -> useCases.chooseScanTypeUseCase(manToken, "Manager",volId));
        String[] code = new String[1];
        evaluateUseCaseDuration(() -> {
            String newCode = useCases.createQrCodeUseCase(manToken, "Manager", volId);
            code[0] = newCode;
            return newCode != null;
        });
        evaluateUseCaseDuration(() -> useCases.scanCodeUseCase(token,USERNAME,code[0]));
    }

    @Test
    public void manualHourApprovalResponseTimeTest(){
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);

        evaluateUseCaseDuration(() -> useCases.makeHourRequestUseCase(token, USERNAME, volId));
        evaluateUseCaseDuration(() -> useCases.approveHoursUseCase(manToken, "Manager", volId,USERNAME));
    }

    @Test
    public void viewSummaryResponseTimeTest(){
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);
        useCases.makeHourRequestUseCase(token, USERNAME, volId);
        useCases.approveHoursUseCase(manToken, "Manager", volId,USERNAME);
        evaluateUseCaseDuration(() -> notNullAndGivenSize(useCases.viewSummaryUseCase(token, USERNAME),1));
    }

    @Test
    public void leaveVolunteeringResponseTimeTest() {
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token, USERNAME, volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken, "Manager", volId, USERNAME);
        evaluateUseCaseDuration(() -> useCases.leaveVolunteeringUseCase(token, USERNAME, volId));
    }

    @Test
    public void exportCsvResponseTimeTest() {
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId = useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken,"Manager",volId);
        int rangeId = useCases.newRangeUseCase(manToken,"Manager",volId,locId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token, USERNAME, volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken, "Manager", volId, USERNAME);
        useCases.chooseVolunteeringRangeUseCase(token,USERNAME,volId,rangeId,locId);

        evaluateUseCaseDuration(() -> {
            try {
                return useCases.exportCsvUseCase(token, USERNAME);
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void exportPdfResponseTimeTest() throws IOException {
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        useCases.uploadSignatureUseCase(manToken, "Manager",orgId);

        useCases.registerStudentUserUseCase(USERNAME);
        useCases.verifyStudent(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);
        useCases.makeHourRequestUseCase(token, USERNAME, volId);
        useCases.approveHoursUseCase(manToken, "Manager", volId,USERNAME);
        evaluateUseCaseDuration(() -> {
            try {
                return useCases.exportPdfUseCase(token, USERNAME,volId);
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void createVolunteerPostResponseTimeTest() {
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        evaluateUseCaseDuration(() -> useCases.newVolunteerPostUseCase(USERNAME, token) >= 0);
    }

    @Test
    public void updateOrganizationDetailsResponseTimeTest() {
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);

        evaluateUseCaseDuration(() -> useCases.updateOrganizationDetailsUseCase(token,USERNAME,orgId));
    }

    @Test
    public void deleteOrganizationDetailsResponseTimeTest() {
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);

        evaluateUseCaseDuration(() -> useCases.deleteOrganizationUseCase(token,USERNAME,orgId));
    }

    @Test
    public void assignLocationResponseTimeTest(){
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);
        int locId = useCases.newLocationUseCase(manToken,"Manager",volId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);

        evaluateUseCaseDuration(() -> useCases.assignVolunteeringLocationUseCase(manToken,"Manager",volId,locId,USERNAME));
    }

    @Test
    public void moveGroupResponseTimeTest(){
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);

        int[] groupId = new int[1];

        evaluateUseCaseDuration(() -> {
            int newGroupId = useCases.createGroupUseCase(manToken,"Manager",volId);
            groupId[0] = newGroupId;
            return newGroupId > 0;
        });

        evaluateUseCaseDuration(() -> useCases.moveGroupUseCase(manToken,"Manager",volId,groupId[0],USERNAME));
    }

    @Test
    public void sendNotificationResponseTimeTest() {
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        int orgId = useCases.newOrganizationUseCase(manToken, "Manager");
        int volId= useCases.newVolunteeringUseCase(manToken, "Manager", orgId);

        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);

        useCases.sendVolunteeringJoinRequestUseCase(token,USERNAME,volId);
        useCases.acceptVolunteeringJoinRequestUseCase(manToken,"Manager",volId,USERNAME);

        evaluateUseCaseDuration(() -> useCases.sendNotificationsUseCase(manToken, "Manager",volId));
    }

    @Test
    public void createVolunteeringPostResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        int volId= useCases.newVolunteeringUseCase(token, USERNAME, orgId);
        evaluateUseCaseDuration(() -> useCases.newVolunteeringPostUseCase(token,USERNAME,volId) >= 0);
    }

    @Test
    public void updateVolunteeringPostResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        int volId= useCases.newVolunteeringUseCase(token, USERNAME, orgId);
        int postId =useCases.newVolunteeringPostUseCase(token,USERNAME,volId);
        evaluateUseCaseDuration(() ->  useCases.updateVolunteeringPostUseCase(token,USERNAME,postId));
    }

    @Test
    public void uploadSignatureResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);
        evaluateUseCaseDuration(() -> {
            try {
                return useCases.uploadSignatureUseCase(token,USERNAME,orgId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void sendManagerRequestResponseTimeTest(){
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int orgId = useCases.newOrganizationUseCase(token, USERNAME);

        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        evaluateUseCaseDuration(() ->  useCases.sendOrganizationManagerRequestUseCase(token,USERNAME,orgId,"Manager"));
        evaluateUseCaseDuration(() ->  useCases.handleOrganizationManagerRequestUseCase(manToken,"Manager",orgId,true));
    }

    @Test
    public void sendVolunteerPostMessageResponseTimeTest() {
        useCases.registerUserUseCase("Manager");
        useCases.verifyUsername("Manager");
        String manToken = useCases.loginUserUseCase("Manager");
        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        int postId = useCases.newVolunteerPostUseCase(USERNAME, token);

        evaluateUseCaseDuration(() -> useCases.sendVolunteerPostChatMessageUseCase(manToken, "Manager",postId,"Manager"));
        evaluateUseCaseDuration(() -> useCases.sendVolunteerPostChatMessageUseCase(token, USERNAME,postId,"Manager"));
    }

    @Test
    public void reportsResponseTimeTest() {
        useCases.registerAdmin("Admin");
        useCases.verifyUsername("Admin");
        String admToken = useCases.loginUserUseCase("Admin");

        useCases.registerUserUseCase("Malicious");
        useCases.verifyUsername("Malicious");
        String malToken = useCases.loginUserUseCase("Malicious");
        int orgId = useCases.newOrganizationUseCase(malToken, "Malicious");
        int volId = useCases.newVolunteeringUseCase(malToken, "Malicious", orgId);
        int volgPostId = useCases.newVolunteeringPostUseCase(malToken,"Malicious",volId);
        int volrPostId = useCases.newVolunteerPostUseCase("Malicious",malToken);



        useCases.registerUserUseCase(USERNAME);
        useCases.verifyUsername(USERNAME);
        String token = useCases.loginUserUseCase(USERNAME);
        evaluateUseCaseDuration(() -> useCases.reportUserUseCase(token,USERNAME,"Malicious"));
        evaluateUseCaseDuration(() -> useCases.reportOrganizationUseCase(token,USERNAME,orgId));
        evaluateUseCaseDuration(() -> useCases.reportVolunteeringUseCase(token,USERNAME,volId));
        evaluateUseCaseDuration(() -> useCases.reportVolunteeringPostUseCase(token,USERNAME,volgPostId));
        evaluateUseCaseDuration(() -> useCases.reportVolunteerPostUseCase(token,USERNAME,volrPostId));

        evaluateUseCaseDuration(() -> notNullAndGivenSize(useCases.watchReportsUseCase(admToken, "Admin"),5));
    }

    @Test
    public void banEmailResponseTimeTest() {
        useCases.registerAdmin("Admin");
        useCases.verifyUsername("Admin");
        String admToken = useCases.loginUserUseCase("Admin");

        useCases.registerStudentUserUseCase("Malicious");

        evaluateUseCaseDuration(() -> useCases.blockEmailUseCase(admToken, "Admin", "email@post.bgu.ac.il"));
    }
}
