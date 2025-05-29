package com.dogood.dogoodbackend.acceptance.nonfunctional;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.posts.PostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.reports.ReportDTO;
import com.dogood.dogoodbackend.domain.volunteerings.AddressTuple;
import com.dogood.dogoodbackend.domain.volunteerings.ApprovalType;
import com.dogood.dogoodbackend.domain.volunteerings.ScanTypes;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Async
public class AsyncUseCases {
    @Autowired
    ChatSocketSender chatSocketSender;

    //THIS IS IMPORTANT DO IT IN ALL ACCEPTANCE TESTS
    @Autowired
    NotificationSocketSender notificationSocketSender;

    //THIS IS ALSO VERY IMPORTANT
    @Autowired
    FirebaseMessaging firebaseMessaging;

    //THE CompletableFuture<Integer>ERNET WILL BREAK IF WE DONT DO THIS IN EVERY ACCEPTANCE TEST
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

    public CompletableFuture<Boolean> registerUserUseCase(String username){
        Response<String> response = userService.register(username, "123456", "Name name", "email@gmail.com", "052-0520520", new Date(), "");
        if(response.getError()){
            System.out.println(response.getErrorString());
        }
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<String> loginUserUseCase(String username){
        Response<String> response = userService.login(username, "123456");
        return CompletableFuture.completedFuture(response.getError() ? null : response.getData());
    }

    public CompletableFuture<Boolean> logoutUserUseCase(String token){
        Response<String> response = userService.logout(token);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> updateUserDetailsUseCase(String token, String username){
        Response<String> response = userService.updateUserFields(token,username,null,null,"Names names", "053-1234567");
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Integer> newOrganizationUseCase(String token, String username){
        Response<Integer> response = organizationService.createOrganization(token,"Name","Description","052-0520520","email@gmail.com",username);
        return CompletableFuture.completedFuture(response.getError() ? -1 : response.getData());
    }

    public CompletableFuture<Boolean> updateUserSkillsUseCase(String token, String username){
        Response<String> response = userService.updateUserSkills(token,username, List.of("Skill1","Skill2"));
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> updateUserPreferencesUseCase(String token, String username){
        Response<String> response = userService.updateUserPreferences(token,username, List.of("Pref1","Pref2"));
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<List<PostDTO>> filterPostsUseCase(String token, String username){
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
        return CompletableFuture.completedFuture(response3.getError() ? null : response3.getData());
    }

    public CompletableFuture<Boolean> updateVolunteeringCategoriesUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.updateVolunteeringCategories(token, username, volunteeringId, List.of("Category1", "Category2"));
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> updateVolunteeringSkillsUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.updateVolunteeringSkills(token, username, volunteeringId, List.of("Skill1", "Skill2"));
        if(response.getError()){
            System.out.println(response.getErrorString());
        }
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> sendVolunteeringJoinRequestUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.requestToJoinVolunteering(token,username,volunteeringId,"");
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> acceptVolunteeringJoinRequestUseCase(String token, String username, int volunteeringId, String sender){
        Response<String> response = volunteeringService.acceptUserJoinRequest(token,username,volunteeringId,sender,0);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> chooseVolunteeringLocationUseCase(String token, String username, int volunteeringId, int locationId){
        Response<String> response = volunteeringService.assignVolunteerToLocation(token,username,username,volunteeringId,locationId);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> chooseVolunteeringRangeUseCase(String token, String username, int volunteeringId, int rangeId, int locId){
        Response<String> response = volunteeringService.
                makeAppointment(token, username, volunteeringId, 0, locId, rangeId,
                        0,0,23,59, null, LocalDate.now());
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> sendVolunteeringChatMessageUseCase(String token, String username, int volunteeringId){
        Response<Integer> response = chatService.sendVolunteeringMessage(token,username, "Message", volunteeringId);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> scanCodeUseCase(String token, String username, String code){
        Response<String> response = volunteeringService.scanCode(token,username,code);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> approveHoursUseCase(String token, String username, int volunteeringId, String approveTo){
        LocalDate now = LocalDate.now();
        LocalTime start = LocalTime.of(10,0);
        LocalTime end = LocalTime.of(12,0);
        Date startDate = Date.from(now.atTime(start).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(end).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> response = volunteeringService.approveUserHours(token,username,volunteeringId,approveTo,startDate,endDate);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<List<HourApprovalRequest>> viewSummaryUseCase(String token, String username){
        Response<List<HourApprovalRequest>> response = userService.getApprovedHours(token,username);
        return CompletableFuture.completedFuture(response.getError() ? null : response.getData());
    }

    public CompletableFuture<Boolean> leaveVolunteeringUseCase(String token, String username, int volunteeringId){
        Response<String> response = volunteeringService.finishVolunteering(token,username,volunteeringId, "Experience");
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> exportCsvUseCase(String token, String username) throws DocumentException, IOException {
        Response<String> response = volunteeringService.getAppointmentsCsv(token,username,12);
        if(response.getError()){
            return CompletableFuture.completedFuture(false);
        }
        File file = new File(response.getData());
        File parentDir =  file.getParentFile();
        file.delete();
        if(parentDir.isDirectory() && parentDir.list().length == 0) {
            parentDir.delete();
        }
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Integer> newVolunteerPostUseCase(String username, String token){
        Response<Integer> response = postService.createVolunteerPost(token, username,"Title", "Description");
        return CompletableFuture.completedFuture(response.getError() ? -1 : response.getData());
    }

    public CompletableFuture<Boolean> exportPdfUseCase(String token, String username, int volunteeringId) throws DocumentException, IOException {
        Response<String> response = volunteeringService.getUserApprovedHoursFormatted(token,username,volunteeringId, "123456789");
        if(response.getError()){
            return CompletableFuture.completedFuture(false);
        }
        File file = new File(response.getData());
        File parentDir =  file.getParentFile();
        file.delete();
        if(parentDir.isDirectory() && parentDir.list().length == 0) {
            parentDir.delete();
        }
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> updateOrganizationDetailsUseCase(String token, String username, int orgId){
        Response<Boolean> response = organizationService.editOrganization(
                token, orgId, "New Name", "New Description", "052-1234052",
                "newmail@gmail.com",username);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Integer> newVolunteeringUseCase(String token, String username, int orgId){
        Response<Integer> response = organizationService.createVolunteering(token, orgId, "Name", "Description", username);
        return CompletableFuture.completedFuture(response.getError() ? -1 : response.getData());
    }

    public CompletableFuture<Integer> newLocationUseCase(String token, String username, int volunteeringId){
        Response<Integer> response = volunteeringService.addVolunteeringLocation(token,username,volunteeringId,
                "Name",new AddressTuple("City", "Street", "Address"));
        return CompletableFuture.completedFuture(response.getError() ? -1 : response.getData());
    }

    public CompletableFuture<Integer> newRangeUseCase(String token, String username, int volunteeringId, int locId){
        Response<Integer> response = volunteeringService.addScheduleRangeToGroup(token,username,volunteeringId,
                0, locId, 0, 0, 23, 59, -1, -1, null, LocalDate.now());
        return CompletableFuture.completedFuture(response.getError() ? -1 : response.getData());
    }

    public CompletableFuture<Boolean> assignVolunteeringLocationUseCase(String token, String username, int volunteeringId, int locId, String volunteer){
        Response<String> response = volunteeringService.assignVolunteerToLocation(token, username, volunteer, volunteeringId, locId);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> moveGroupUseCase(String token, String username, int volunteeringId, int groupId, String volunteer){
        Response<String> response = volunteeringService.moveVolunteerGroup(token,username,volunteer,volunteeringId,groupId);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Integer> newVolunteeringPostUseCase(String token, String username, int volunteeringId){
        Response<Integer> response = postService.createVolunteeringPost(token, "Title", "Description", username, volunteeringId);
        return CompletableFuture.completedFuture(response.getError() ? -1 : response.getData());
    }

    public CompletableFuture<Boolean> updateVolunteeringPostUseCase(String token, String username, int postId){
        Response<Boolean> response = postService.editVolunteeringPost(token,postId, "New Title", "New Description", username);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> sendOrganizationManagerRequestUseCase(String token, String username, int organizationId, String to){
        Response<Boolean> response = organizationService.sendAssignManagerRequest(token, to, username, organizationId);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> sendVolunteerPostChatMessageUseCase(String token, String username, int postId, String with){
        Response<Integer> response = chatService.sendPostMessage(token,username, "Message", postId,with);
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> reportVolunteeringUseCase(String token, String username, int volunteeringId){
        Response<ReportDTO> response = reportService.createVolunteeringReport(token,username, volunteeringId, "Description");
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> reportVolunteerPostUseCase(String token, String username, int postId){
        Response<ReportDTO> response = reportService.createVolunteerPostReport(token,username, postId, "Description");
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> reportOrganizationUseCase(String token, String username, int organizationId){
        Response<ReportDTO> response = reportService.createOrganizationReport(token,username, organizationId, "Description");
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> reportUserUseCase(String token, String username, String userId){
        Response<ReportDTO> response = reportService.createUserReport(token,username, userId, "Description");
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> reportVolunteeringPostUseCase(String token, String username, int postId){
        Response<ReportDTO> response = reportService.createVolunteeringPostReport(token,username, postId, "Description");
        return CompletableFuture.completedFuture(!response.getError());
    }

    public CompletableFuture<Boolean> blockEmailUseCase(String token, String username, String emailToBlock){
        Response<Boolean> response = reportService.banEmail(token, username, emailToBlock);
        return CompletableFuture.completedFuture(!response.getError());
    }
}
