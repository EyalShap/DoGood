package com.dogood.dogoodbackend.acceptance.nonfunctional;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.posts.Post;
import com.dogood.dogoodbackend.domain.posts.PostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.domain.volunteerings.AddressTuple;
import com.dogood.dogoodbackend.domain.volunteerings.ApprovalType;
import com.dogood.dogoodbackend.domain.volunteerings.ScanTypes;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.emailverification.VerificationData;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.*;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itextpdf.text.DocumentException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class UseCases {
    @Autowired
    ChatSocketSender chatSocketSender;

    //THIS IS IMPORTANT DO IT IN ALL ACCEPTANCE TESTS
    @Autowired
    NotificationSocketSender notificationSocketSender;

    //THIS IS ALSO VERY IMPORTANT
    @Autowired
    FirebaseMessaging firebaseMessaging;

    //THE INTERNET WILL BREAK IF WE DONT DO THIS IN EVERY ACCEPTANCE TEST
    @Autowired
    Gemini gemini;

    @Autowired
    EmailSender emailSender;

    @Autowired
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


    @Autowired
    FacadeManager facadeManager;
    @Autowired
    private ReportService reportService;

    public void reset(){
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
    }

    public boolean registerUserUseCase(String username){
        Response<String> response = userService.register(username, "123456", "Name name", "email@gmail.com", "052-0520520", new Date(), "");
        return !response.getError();
    }

    public boolean registerStudentUserUseCase(String username){
        Response<String> response = userService.register(username, "123456", "Name name", "email@post.bgu.ac.il", "052-0520520", new Date(), "");
        return !response.getError();
    }

    public void verifyStudent(String username){
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(username),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("email@post.bgu.ac.il");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        VerifyEmailRequest emailRequest = new VerifyEmailRequest(username,"");
        userService.verifyEmail(emailRequest);
    }


    public void verifyUsername(String username){
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(username),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("email@gmail.com");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        VerifyEmailRequest emailRequest = new VerifyEmailRequest(username,"");
        userService.verifyEmail(emailRequest);
    }

    public String loginUserUseCase(String username){
        Response<String> response = userService.login(username, "123456");
        return response.getError() ? null : response.getData();
    }

    public boolean logoutUserUseCase(String token){
        Response<String> response = userService.logout(token);
        return !response.getError();
    }

    public boolean updateUserDetailsUseCase(String token, String username){
        Response<String> response = userService.updateUserFields(token,username,null,null,"Names names", "053-1234567");
        return !response.getError();
    }

    public int newOrganizationUseCase(String token, String username){
        Response<Integer> response = organizationService.createOrganization(token,"Name","Description","052-0520520","email@gmail.com",username);
        return response.getError() ? -1 : response.getData();
    }

    public boolean updateUserSkillsUseCase(String token, String username){
        Response<String> response = userService.updateUserSkills(token,username, List.of("Skill1","Skill2"));
        return !response.getError();
    }

    public boolean updateUserPreferencesUseCase(String token, String username){
        Response<String> response = userService.updateUserPreferences(token,username, List.of("Pref1","Pref2"));
        return !response.getError();
    }

    public List<PostDTO> filterPostsUseCase(String token, String username){
        Response<List<VolunteeringPostDTO>> response0 = postService.getAllVolunteeringPosts(token,username);
        if(response0.getError()){
            return null;
        }
        Response<List<? extends PostDTO>> response1 = postService.searchByKeywords(token,"Keyword", username,
                response0.getData().stream().map(dto -> (PostDTO)dto).toList()
                , true);
        if(response1.getError()){
            return null;
        }
        Response<List<VolunteeringPostDTO>> response2 =
                postService.filterVolunteeringPosts(token,
                        Set.of("Category1"), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(),
                        username, response1.getData().stream().map(post -> post.getId()).toList(), false);
        if(response2.getError()){
            return null;
        }
        Response<List<PostDTO>> response3 = postService.sortByPostingTime(token,username,
                response2.getData().stream().map(dto -> (PostDTO)dto).toList());
        return response3.getError() ? null : response3.getData();
    }

    public boolean updateVolunteeringCategoriesUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.updateVolunteeringCategories(token, username, volunteeringId, List.of("Category1", "Category2"));
        return !response.getError();
    }

    public boolean updateVolunteeringSkillsUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.updateVolunteeringSkills(token, username, volunteeringId, List.of("Skill1", "Skill2"));
        return !response.getError();
    }

    public boolean sendVolunteeringJoinRequestUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.requestToJoinVolunteering(token,username,volunteeringId,"");
        return !response.getError();
    }

    public boolean acceptVolunteeringJoinRequestUseCase(String token, String username, int volunteeringId, String sender){
        Response<String> response = volunteeringService.acceptUserJoinRequest(token,username,volunteeringId,sender,0);
        return !response.getError();
    }

    public boolean chooseVolunteeringLocationUseCase(String token, String username, int volunteeringId, int locationId){
        Response<String> response = volunteeringService.assignVolunteerToLocation(token,username,username,volunteeringId,locationId);
        return !response.getError();
    }

    public boolean chooseVolunteeringRangeUseCase(String token, String username, int volunteeringId, int rangeId, int locId){
        Response<String> response = volunteeringService.
                makeAppointment(token, username, volunteeringId, 0, locId, rangeId,
                        0,0,23,59, null, LocalDate.now());
        return !response.getError();
    }

    public boolean sendVolunteeringChatMessageUseCase(String token, String username, int volunteeringId){
        Response<Integer> response = chatService.sendVolunteeringMessage(token,username, "Message", volunteeringId);
        return !response.getError();
    }

    public boolean chooseScanTypeUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.updateVolunteeringScanDetails(token,username,volunteeringId, ScanTypes.ONE_SCAN, ApprovalType.AUTO_FROM_SCAN);
        return !response.getError();
    }

    public String createQrCodeUseCase(String token, String username,int volunteeringId){
        Response<String> response = volunteeringService.makeVolunteeringCode(token,username,volunteeringId,false);
        return response.getError() ? null : response.getData();
    }

    public boolean scanCodeUseCase(String token, String username, String code){
        Response<String> response = volunteeringService.scanCode(token,username,code);
        return !response.getError();
    }

    public boolean makeHourRequestUseCase(String token, String username, int volunteeringId){
        LocalDate now = LocalDate.now();
        LocalTime start = LocalTime.of(10,0);
        LocalTime end = LocalTime.of(12,0);
        Date startDate = Date.from(now.atTime(start).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(end).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> response = volunteeringService.
                requestHoursApproval(token,username, volunteeringId,startDate,endDate);
        return !response.getError();
    }

    public boolean approveHoursUseCase(String token, String username, int volunteeringId, String approveTo){
        LocalDate now = LocalDate.now();
        LocalTime start = LocalTime.of(10,0);
        LocalTime end = LocalTime.of(12,0);
        Date startDate = Date.from(now.atTime(start).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(end).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> response = volunteeringService.approveUserHours(token,username,volunteeringId,approveTo,startDate,endDate);
        return !response.getError();
    }

    public List<HourApprovalRequest> viewSummaryUseCase(String token, String username){
        Response<List<HourApprovalRequest>> response = userService.getApprovedHours(token,username);
        return response.getError() ? null : response.getData();
    }

    public boolean leaveVolunteeringUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.finishVolunteering(token,username,volunteeringId, "Experience");
        return !response.getError();
    }

    public boolean exportCsvUseCase(String token, String username) throws DocumentException, IOException {
        Response<String> response = volunteeringService.getAppointmentsCsv(token,username,12);
        if(response.getError()){
            return false;
        }
        File file = new File(response.getData());
        File parentDir =  file.getParentFile();
        file.delete();
        if(parentDir.isDirectory() && parentDir.list().length == 0) {
            parentDir.delete();
        }
        return !response.getError();
    }

    public int newVolunteerPostUseCase(String username, String token){
        Response<Integer> response = postService.createVolunteerPost(token, username,"Title", "Description");
        return response.getError() ? -1 : response.getData();
    }

    public boolean exportPdfUseCase(String token, String username, int volunteeringId) throws DocumentException, IOException {
        Response<String> response = volunteeringService.getUserApprovedHoursFormatted(token,username,volunteeringId, "123456789");
        if(response.getError()){
            return false;
        }
        File file = new File(response.getData());
        File parentDir =  file.getParentFile();
        file.delete();
        if(parentDir.isDirectory() && parentDir.list().length == 0) {
            parentDir.delete();
        }
        return !response.getError();
    }

    public boolean updateOrganizationDetailsUseCase(String token, String username, int orgId){
        Response<Boolean> response = organizationService.editOrganization(
                token, orgId, "New Name", "New Description", "052-1234052",
                "newmail@gmail.com",username);
        return !response.getError();
    }

    public boolean deleteOrganizationUseCase(String token, String username, int orgId){
        Response<Boolean> response = organizationService.removeOrganization(token,orgId,username);
        return !response.getError();
    }

    public int newVolunteeringUseCase(String token, String username, int orgId){
        Response<Integer> response = organizationService.createVolunteering(token, orgId, "Name", "Description", username);
        return response.getError() ? -1 : response.getData();
    }

    public int newLocationUseCase(String token, String username, int volunteeringId){
        Response<Integer> response = volunteeringService.addVolunteeringLocation(token,username,volunteeringId,
                "Name",new AddressTuple("City", "Street", "Address"));
        return response.getError() ? -1 : response.getData();
    }

    public int newRangeUseCase(String token, String username, int volunteeringId, int locId){
        Response<Integer> response = volunteeringService.addScheduleRangeToGroup(token,username,volunteeringId,
                0, locId, 0, 0, 23, 59, -1, -1, null, LocalDate.now());
        return response.getError() ? -1 : response.getData();
    }

    public boolean assignVolunteeringLocationUseCase(String token, String username, int volunteeringId, int locId, String volunteer){
        Response<String> response = volunteeringService.assignVolunteerToLocation(token, username, volunteer, volunteeringId, locId);
        return !response.getError();
    }

    public int createGroupUseCase(String token, String username, int volunteeringId){
        Response<Integer> response = volunteeringService.createNewGroup(token,username,volunteeringId);
        return response.getError() ? -1 : response.getData();
    }

    public boolean moveGroupUseCase(String token, String username, int volunteeringId, int groupId, String volunteer){
        Response<String> response = volunteeringService.moveVolunteerGroup(token,username,volunteer,volunteeringId,groupId);
        return !response.getError();
    }

    public boolean sendNotificationsUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.sendUpdatesToVolunteers(token, username, volunteeringId, "Message");
        return !response.getError();
    }

    public int newVolunteeringPostUseCase(String token, String username, int volunteeringId){
        Response<Integer> response = postService.createVolunteeringPost(token, "Title", "Description", username, volunteeringId);
        return response.getError() ? -1 : response.getData();
    }

    public boolean updateVolunteeringPostUseCase(String token, String username, int postId){
        Response<Boolean> response = postService.editVolunteeringPost(token,postId, "New Title", "New Description", username);
        return !response.getError();
    }

    public boolean uploadSignatureUseCase(String token, String username, int organizationId) throws IOException {
        File signature = ResourceUtils.getFile("classpath:signature-example.png");
        FileInputStream signatureStream = new FileInputStream(signature);
        Response<Boolean> response = organizationService.uploadSignature(token, organizationId, username,new MockMultipartFile("signature-example.png", signatureStream));
        return !response.getError();
    }

    public boolean sendOrganizationManagerRequestUseCase(String token, String username, int organizationId, String to){
        Response<Boolean> response = organizationService.sendAssignManagerRequest(token, to, username, organizationId);
        return !response.getError();
    }

    public boolean handleOrganizationManagerRequestUseCase(String token, String username, int organizationId, boolean approved){
        Response<Boolean> response = organizationService.handleAssignManagerRequest(token,username,organizationId,approved);
        return !response.getError();
    }

    public boolean sendVolunteerPostChatMessageUseCase(String token, String username, int postId, String with){
        Response<Integer> response = chatService.sendPostMessage(token,username, "Message", postId,with);
        return !response.getError();
    }

    public void registerAdmin(String username){
        facadeManager.getUsersFacade().registerAdmin(username, "123456", "Admin adm", "email@gmail.com", "052-0520520", new Date());
    }

    public List<ReportDTO> watchReportsUseCase(String token, String username){
        Response<List<ReportDTO>> response = reportService.getAllReportDTOs(token,username);
        return response.getError() ? null : response.getData();
    }

    public boolean reportVolunteeringUseCase(String token, String username, int volunteeringId){
        Response<ReportDTO> response = reportService.createVolunteeringReport(token,username, volunteeringId, "Description");
        return !response.getError();
    }

    public boolean reportVolunteerPostUseCase(String token, String username, int postId){
        Response<ReportDTO> response = reportService.createVolunteerPostReport(token,username, postId, "Description");
        return !response.getError();
    }

    public boolean reportOrganizationUseCase(String token, String username, int organizationId){
        Response<ReportDTO> response = reportService.createOrganizationReport(token,username, organizationId, "Description");
        return !response.getError();
    }

    public boolean reportUserUseCase(String token, String username, String userId){
        Response<ReportDTO> response = reportService.createUserReport(token,username, userId, "Description");
        return !response.getError();
    }

    public boolean reportVolunteeringPostUseCase(String token, String username, int postId){
        Response<ReportDTO> response = reportService.createVolunteeringPostReport(token,username, postId, "Description");
        return !response.getError();
    }

    public boolean blockEmailUseCase(String token, String username, String emailToBlock){
        Response<Boolean> response = reportService.banEmail(token, username, emailToBlock);
        return !response.getError();
    }
}
