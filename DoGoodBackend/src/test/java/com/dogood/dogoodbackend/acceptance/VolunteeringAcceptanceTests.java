package com.dogood.dogoodbackend.acceptance;

import com.dogood.dogoodbackend.api.userrequests.RegisterRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.VolunteeringInHistory;
import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointmentDTO;
import com.dogood.dogoodbackend.emailverification.EmailSender;
import com.dogood.dogoodbackend.emailverification.VerificationCacheService;
import com.dogood.dogoodbackend.emailverification.VerificationData;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.*;
import com.dogood.dogoodbackend.socket.ChatSocketSender;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.database.core.Repo;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class VolunteeringAcceptanceTests {
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

    private String organizationMangerId;
    private String aliceId;
    private String bobId;
    private String bguStudentId;
    private String tauStudentId;

    private String organizationManagerToken;
    private String aliceToken;
    private String bobToken;
    private String bguStudentToken;
    private String tauStudentToken;

    private int organizationId;
    private int volunteeringId;


    @BeforeEach
    public void setUp(){
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

        organizationMangerId = "OrgMan";
        aliceId = "Alice";
        bobId = "Bobb";
        bguStudentId = "BguStudent";
        tauStudentId = "TauStudent";
        userService.register(organizationMangerId,
                "123456",
                "Organization Manager",
                "organization@manager.com",
                "052-0520520",
                new Date(),
                null);
        userService.register(aliceId,
                "123456",
                "Alice Alice",
                "alice@dogood.com",
                "052-0520520",
                new Date(),
                null);
        userService.register(bobId,
                "123456",
                "Bob Bob",
                "bob@dogood.com",
                "052-0520520",
                new Date(),
                null);
        userService.register(bguStudentId,
                "123456",
                "BGU Student",
                "student@post.bgu.ac.il",
                "052-0520520",
                new Date(),
                null);
        userService.register(tauStudentId,
                "123456",
                "TAU Student",
                "student@tau.ac.il",
                "052-0520520",
                new Date(),
                null);
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(organizationMangerId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("organization@manager.com");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(aliceId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("alice@dogood.com");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(bobId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("bob@dogood.com");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(bguStudentId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("student@post.bgu.ac.il");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        Mockito.when(verificationCacheService.getAndValidateVerificationData(Mockito.eq(tauStudentId),Mockito.anyString()))
                .thenAnswer(i -> {
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername(i.getArgument(0));
                    request.setEmail("student@tau.ac.il");
                    return Optional.of(new VerificationData("", Instant.MAX,request));
                });
        VerifyEmailRequest orgEmailRequest = new VerifyEmailRequest(organizationMangerId,"");
        VerifyEmailRequest alEmailRequest = new VerifyEmailRequest(aliceId,"");
        VerifyEmailRequest bobEmailRequest = new VerifyEmailRequest(bobId,"");
        VerifyEmailRequest bguEmailRequest = new VerifyEmailRequest(bguStudentId,"");
        VerifyEmailRequest tauEmailRequest = new VerifyEmailRequest(tauStudentId,"");
        userService.verifyEmail(orgEmailRequest);
        userService.verifyEmail(alEmailRequest);
        userService.verifyEmail(bobEmailRequest);
        userService.verifyEmail(bguEmailRequest);
        userService.verifyEmail(tauEmailRequest);

        Response<String> login1 = userService.login(organizationMangerId, "123456");
        Response<String> login2 = userService.login(aliceId, "123456");
        Response<String> login3 = userService.login(bobId, "123456");
        Response<String> login4 = userService.login(bguStudentId, "123456");
        Response<String> login5 = userService.login(tauStudentId, "123456");
        organizationManagerToken = login1.getData();
        aliceToken = login2.getData();
        bobToken = login3.getData();
        bguStudentToken = login4.getData();
        tauStudentToken = login5.getData();

        Response<Integer> createOrganization = organizationService.createOrganization(organizationManagerToken,
                "Organization",
                "Description",
                "052-0520520",
                "organization@manager.com",
                organizationMangerId);
        organizationId = createOrganization.getData();
        Response<Integer> createVolunteering = organizationService.createVolunteering(organizationManagerToken,
                organizationId, "Volunteering", "Description", organizationMangerId);
        volunteeringId = createVolunteering.getData();
    }

    private void joinUserToVolunteering(String token, String userId){
        volunteeringService.requestToJoinVolunteering(token,userId,volunteeringId,"");
        volunteeringService.acceptUserJoinRequest(organizationManagerToken,organizationMangerId,volunteeringId,userId,0);
    }

    @Test
    //names differ from testing document
    //TESTING DOCUMENT - THIS TEST
    //Alice - Organization Manager
    //Bob - Alice
    //Garry - Bob
    //this was done to maintain consistency across tests
    public void givenLocationAndRangeExist_whenChooseLocationAndRange_thenSuccess(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        Response<String> makeAppointment =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,10,0,12,0,
                        new boolean[]{true,false,false,false,false,false,false},null);
        Assertions.assertFalse(makeAppointment.getError());
        Response<Integer> getUserAssignedLocation =
                volunteeringService.getUserAssignedLocation(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getUserAssignedLocation.getError());
        int assignedLocationId = getUserAssignedLocation.getData();
        Assertions.assertEquals(locationId,assignedLocationId);
        Response<List<ScheduleAppointmentDTO>> getVolunteerAppointments =
                volunteeringService.getVolunteerAppointments(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getVolunteerAppointments.getError());
        List<ScheduleAppointmentDTO> appointments = getVolunteerAppointments.getData();
        Assertions.assertEquals(1, appointments.size());
        ScheduleAppointmentDTO appointment = appointments.get(0);
        Assertions.assertEquals(rangeId,appointment.getRangeId());
    }

    @Test
    public void givenLocationDoesntExist_whenChooseLocationAndRange_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId+1);
        Assertions.assertTrue(assignVolunteerToLocation.getError());
        Assertions.assertEquals("There is no location with id " + (locationId+1),assignVolunteerToLocation.getErrorString());
        Response<Integer> getUserAssignedLocation =
                volunteeringService.getUserAssignedLocation(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getUserAssignedLocation.getError());
        int assignedLocationId = getUserAssignedLocation.getData();
        Assertions.assertEquals(-2,assignedLocationId);
    }

    @Test
    public void givenNonMatchingAppointmentRange_whenChooseLocationAndRange_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        Response<String> makeAppointment =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,10,0,12,0,
                        new boolean[]{false,true,false,false,false,false,false},null);
        Assertions.assertTrue(makeAppointment.getError());
        Assertions.assertEquals("Appointment days do not match range days",makeAppointment.getErrorString());
        Response<List<ScheduleAppointmentDTO>> getVolunteerAppointments =
                volunteeringService.getVolunteerAppointments(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getVolunteerAppointments.getError());
        List<ScheduleAppointmentDTO> appointments = getVolunteerAppointments.getData();
        Assertions.assertEquals(0, appointments.size());
        Response<Integer> getUserAssignedLocation =
                volunteeringService.getUserAssignedLocation(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getUserAssignedLocation.getError());
        int assignedLocationId = getUserAssignedLocation.getData();
        Assertions.assertEquals(locationId,assignedLocationId);
    }

    @Test
    public void givenNotVolunteer_whenChooseLocationAndRange_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(bobToken,bobId,bobId,volunteeringId,locationId);
        Assertions.assertTrue(assignVolunteerToLocation.getError());
        Assertions.assertEquals("User " + bobId + " is not a volunteer in volunteering " + volunteeringId,assignVolunteerToLocation.getErrorString());
    }

    @Test
    public void givenRestrictionFull_whenChooseLocationAndRange_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        joinUserToVolunteering(bobToken,bobId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        int rangeId = addScheduleRangeToGroup.getData();
        volunteeringService.addRestrictionToRange(organizationManagerToken,organizationMangerId,volunteeringId,
                0,locationId,rangeId,10,0,12,0,1);
        Response<String> assignVolunteerToLocation1 =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation1.getError());
        Response<String> makeAppointment1 =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,10,0,12,0,
                        new boolean[]{true,false,false,false,false,false,false},null);
        Assertions.assertFalse(makeAppointment1.getError());
        Response<String> assignVolunteerToLocation2 =
                volunteeringService.assignVolunteerToLocation(bobToken,bobId,bobId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation2.getError());
        Response<String> makeAppointment2 =
                volunteeringService.makeAppointment(bobToken
                        ,bobId,volunteeringId,0,locationId,rangeId,10,0,11,0,
                        new boolean[]{true,false,false,false,false,false,false},null);
        Assertions.assertTrue(makeAppointment2.getError());
        Assertions.assertEquals("The range " + rangeId + " between 10:00 and 11:00" + " is full on the specified dates",makeAppointment2.getErrorString());
    }

    @Test
    public void givenThreeWeeksAhead_whenExportCsv_thenAppointmentThreeTimes() throws DocumentException, IOException {
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        Response<String> makeAppointment =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,10,0,12,0,
                        new boolean[]{true,false,false,false,false,false,false},null);
        Assertions.assertFalse(makeAppointment.getError());
        Response<String> getAppointmentsCsv =
                volunteeringService.getAppointmentsCsv(aliceToken,aliceId,3);
        Assertions.assertFalse(getAppointmentsCsv.getError());
        String fileLocation = getAppointmentsCsv.getData();
        File file = new File(fileLocation);
        List<String> rows = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line);
            }
        }
        File parentDir =  file.getParentFile();
        file.delete();
        if(parentDir.isDirectory() && parentDir.list().length == 0) {
            parentDir.delete();
        }

        Assertions.assertEquals(4,rows.size());
        List<LocalDate> dates = new LinkedList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        for(String row : rows) {
            if(!row.startsWith("Subject")){
                String[] splits = row.split(",");
                Assertions.assertEquals("Volunteering",splits[0]);
                Assertions.assertEquals("10:00 AM",splits[2]);
                Assertions.assertEquals("12:00 PM",splits[3]);
                dates.add(LocalDate.parse(splits[1], formatter));
            }
        }
        Assertions.assertEquals(7, dates.get(0).getDayOfWeek().getValue());
        for(int i = 0 ; i < dates.size()-1; i++) {
            Assertions.assertEquals(7, ChronoUnit.DAYS.between(dates.get(i),dates.get(i+1)),7);
        }
    }

    @Test
    public void givenNoAppointments_whenExportCsv_thenCsvEmpty() throws DocumentException, IOException {
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        Response<String> getAppointmentsCsv =
                volunteeringService.getAppointmentsCsv(aliceToken,aliceId,3);
        Assertions.assertFalse(getAppointmentsCsv.getError());
        String fileLocation = getAppointmentsCsv.getData();
        File file = new File(fileLocation);
        List<String> rows = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line);
            }
        }
        File parentDir =  file.getParentFile();
        file.delete();
        if(parentDir.isDirectory() && parentDir.list().length == 0) {
            parentDir.delete();
        }

        Assertions.assertEquals(1,rows.size());
    }

    @Test
    public void givenNegativeWeeksAhead_whenExportCsv_thenError() throws DocumentException, IOException {
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        Response<String> makeAppointment =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,10,0,12,0,
                        new boolean[]{true,false,false,false,false,false,false},null);
        Assertions.assertFalse(makeAppointment.getError());
        Response<String> getAppointmentsCsv =
                volunteeringService.getAppointmentsCsv(aliceToken,aliceId,-1);
        Assertions.assertTrue(getAppointmentsCsv.getError());
        Assertions.assertEquals("Number of weeks cannot be negative",getAppointmentsCsv.getErrorString());
    }

    @Test
    public void givenValid_whenScanCode_thenRequestHourApproval() {
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,59,-1,-1,null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        volunteeringService.updateVolunteeringScanDetails(organizationManagerToken,organizationMangerId,volunteeringId, ScanTypes.ONE_SCAN, ApprovalType.MANUAL);
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        LocalTime now = LocalTime.now();
        Response<String> makeAppointment =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,now.getHour(),0,now.getHour() < 22 ? now.getHour() + 2 : 23,now.getHour() < 22 ? 0 : 59, null, LocalDate.now());
        Assertions.assertFalse(makeAppointment.getError());
        Response<String> makeVolunteeringCode = volunteeringService.makeVolunteeringCode(organizationManagerToken,organizationMangerId,volunteeringId,false);
        Assertions.assertFalse(makeVolunteeringCode.getError());
        String code = makeVolunteeringCode.getData();
        Response<String> scanCode = volunteeringService.scanCode(aliceToken,aliceId,code);
        Assertions.assertFalse(scanCode.getError());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(1,hourApprovalRequests.size());
        HourApprovalRequest request = hourApprovalRequests.get(0);
        Instant instant = now.withMinute(0).withSecond(0).withNano(0).atDate(LocalDate.now()).
                atZone(ZoneId.systemDefault()).toInstant();
        Date startTime = Date.from(instant);
        Assertions.assertEquals(startTime.getTime(),request.getStartTime().getTime());
    }

    @Test
    public void givenExpired_whenScanCode_thenError() throws InterruptedException {
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,59,-1,-1,null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        volunteeringService.updateVolunteeringScanDetails(organizationManagerToken,organizationMangerId,volunteeringId, ScanTypes.ONE_SCAN, ApprovalType.MANUAL);
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        LocalTime now = LocalTime.now();
        Response<String> makeAppointment =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,now.getHour(),0,now.getHour() < 22 ? now.getHour() + 2 : 23,now.getHour() < 22 ? 0 : 59, null, LocalDate.now());
        Assertions.assertFalse(makeAppointment.getError());
        Response<String> makeVolunteeringCode = volunteeringService.makeVolunteeringCode(organizationManagerToken,organizationMangerId,volunteeringId,false);
        Assertions.assertFalse(makeVolunteeringCode.getError());
        String code = makeVolunteeringCode.getData();
        System.out.println("Waiting for 15 seconds, don't panic");
        Thread.sleep(15000);
        Response<String> scanCode = volunteeringService.scanCode(aliceToken,aliceId,code);
        Assertions.assertTrue(scanCode.getError());
        Assertions.assertEquals("Invalid code",scanCode.getErrorString());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(0,hourApprovalRequests.size());
    }

    @Test
    public void givenAppointmentPassed_whenScanCode_thenError() {
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,59,-1,-1,null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        volunteeringService.updateVolunteeringScanDetails(organizationManagerToken,organizationMangerId,volunteeringId, ScanTypes.ONE_SCAN, ApprovalType.MANUAL);
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        LocalTime now = LocalTime.now();
        int startHour;
        int endHour;
        if(now.getHour() == 0){
            startHour = now.getHour()+1;
            endHour = now.getHour()+2;
        }else{
            startHour = now.getHour()-2;
            endHour = now.getHour()-1;
        }
        Response<String> makeAppointment =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,startHour,0,endHour, 0,null, LocalDate.now());
        Assertions.assertFalse(makeAppointment.getError());
        Response<String> makeVolunteeringCode = volunteeringService.makeVolunteeringCode(organizationManagerToken,organizationMangerId,volunteeringId,false);
        Assertions.assertFalse(makeVolunteeringCode.getError());
        String code = makeVolunteeringCode.getData();
        Response<String> scanCode = volunteeringService.scanCode(aliceToken,aliceId,code);
        Assertions.assertTrue(scanCode.getError());
        Assertions.assertEquals("Appointment not found for the specified scan times in volunteering " + volunteeringId + " for user " + aliceId + ", please request manually",scanCode.getErrorString());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(0,hourApprovalRequests.size());
    }

    @Test
    public void givenScanNotEnabled_whenScanCode_thenError() {
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,59,-1,-1,null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(aliceToken,aliceId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        LocalTime now = LocalTime.now();
        Response<String> makeAppointment =
                volunteeringService.makeAppointment(aliceToken
                        ,aliceId,volunteeringId,0,locationId,rangeId,now.getHour(),0,now.getHour() < 22 ? now.getHour() + 2 : 23,now.getHour() < 22 ? 0 : 59, null, LocalDate.now());
        Assertions.assertFalse(makeAppointment.getError());
        Response<String> makeVolunteeringCode = volunteeringService.makeVolunteeringCode(organizationManagerToken,organizationMangerId,volunteeringId,false);
        Assertions.assertFalse(makeVolunteeringCode.getError());
        String code = makeVolunteeringCode.getData();
        Response<String> scanCode = volunteeringService.scanCode(aliceToken,aliceId,code);
        Assertions.assertTrue(scanCode.getError());
        Assertions.assertEquals("Volunteering " + volunteeringId + " does not support QR codes",scanCode.getErrorString());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(0,hourApprovalRequests.size());
    }

    @Test
    public void givenValid_whenManualHourRequest_thenRequestHourApprovalAndApprove() {
        joinUserToVolunteering(aliceToken,aliceId);
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(14,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> requestHoursApproval = volunteeringService
                .requestHoursApproval(aliceToken,aliceId,volunteeringId,startTime,endTime);
        Assertions.assertFalse(requestHoursApproval.getError());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(1,hourApprovalRequests.size());
        HourApprovalRequest request = hourApprovalRequests.get(0);
        Assertions.assertEquals(startTime.getTime(),request.getStartTime().getTime());
        Assertions.assertEquals(endTime.getTime(),request.getEndTime().getTime());
        Response<String> approveUserHours = volunteeringService
                .approveUserHours(organizationManagerToken,organizationMangerId,volunteeringId,aliceId,startTime,endTime);
        Assertions.assertFalse(approveUserHours.getError());
        Response<List<HourApprovalRequest>> getApprovedHours = userService.getApprovedHours(aliceToken,aliceId);
        Assertions.assertFalse(getApprovedHours.getError());
        List<HourApprovalRequest> approvedHours = getApprovedHours.getData();
        Assertions.assertEquals(1,approvedHours.size());
        HourApprovalRequest approved = approvedHours.get(0);
        Assertions.assertTrue(approved.isApproved());
        Assertions.assertEquals(startTime.getTime(),approved.getStartTime().getTime());
        Assertions.assertEquals(endTime.getTime(),approved.getEndTime().getTime());
    }

    @Test
    public void givenWrongApprovedHours_whenManualHourRequest_thenError() {
        joinUserToVolunteering(aliceToken,aliceId);
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(14,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date badTime = Date.from(LocalTime.of(16,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> requestHoursApproval = volunteeringService
                .requestHoursApproval(aliceToken,aliceId,volunteeringId,startTime,endTime);
        Assertions.assertFalse(requestHoursApproval.getError());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(1,hourApprovalRequests.size());
        HourApprovalRequest request = hourApprovalRequests.get(0);
        Assertions.assertEquals(startTime.getTime(),request.getStartTime().getTime());
        Assertions.assertEquals(endTime.getTime(),request.getEndTime().getTime());
        Response<String> approveUserHours = volunteeringService
                .approveUserHours(organizationManagerToken,organizationMangerId,volunteeringId,aliceId,endTime,badTime);
        Assertions.assertTrue(approveUserHours.getError());
        Assertions.assertEquals("There is no hour approval request for user " + aliceId + " in volunteering " + volunteeringId + " from " + endTime + " to " + badTime,
                approveUserHours.getErrorString());
        Response<List<HourApprovalRequest>> getApprovedHours = userService.getApprovedHours(aliceToken,aliceId);
        Assertions.assertFalse(getApprovedHours.getError());
        List<HourApprovalRequest> approvedHours = getApprovedHours.getData();
        Assertions.assertEquals(0,approvedHours.size());
    }

    @Test
    public void givenNotManager_whenManualHourRequest_thenError() {
        joinUserToVolunteering(aliceToken,aliceId);
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(14,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> requestHoursApproval = volunteeringService
                .requestHoursApproval(aliceToken,aliceId,volunteeringId,startTime,endTime);
        Assertions.assertFalse(requestHoursApproval.getError());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(1,hourApprovalRequests.size());
        HourApprovalRequest request = hourApprovalRequests.get(0);
        Assertions.assertEquals(startTime.getTime(),request.getStartTime().getTime());
        Assertions.assertEquals(endTime.getTime(),request.getEndTime().getTime());
        Response<String> approveUserHours = volunteeringService
                .approveUserHours(bobToken,bobId,volunteeringId,aliceId,startTime,endTime);
        Assertions.assertTrue(approveUserHours.getError());
        Assertions.assertEquals("User " + bobId + " is not a manager in organization " + organizationId + " of volunteering " + volunteeringId,
                approveUserHours.getErrorString());
        Response<List<HourApprovalRequest>> getApprovedHours = userService.getApprovedHours(aliceToken,aliceId);
        Assertions.assertFalse(getApprovedHours.getError());
        List<HourApprovalRequest> approvedHours = getApprovedHours.getData();
        Assertions.assertEquals(0,approvedHours.size());
    }

    @Test
    public void givenNotVolunteer_whenManualHourRequest_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(14,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> requestHoursApproval = volunteeringService
                .requestHoursApproval(bobToken,bobId,volunteeringId,startTime,endTime);
        Assertions.assertTrue(requestHoursApproval.getError());
        Assertions.assertEquals("User " + bobId + " is not a volunteer in volunteering " + volunteeringId, requestHoursApproval.getErrorString());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(0,hourApprovalRequests.size());
    }

    @Test
    public void givenEndBeforeStart_whenManualHourRequest_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(1,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Response<String> requestHoursApproval = volunteeringService
                .requestHoursApproval(aliceToken,aliceId,volunteeringId,startTime,endTime);
        Assertions.assertTrue(requestHoursApproval.getError());
        Assertions.assertEquals("End time cannot be before start time", requestHoursApproval.getErrorString());
        Response<List<HourApprovalRequest>> getVolunteeringHourRequests =
                volunteeringService.getVolunteeringHourRequests(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringHourRequests.getError());
        List<HourApprovalRequest> hourApprovalRequests = getVolunteeringHourRequests.getData();
        Assertions.assertEquals(0,hourApprovalRequests.size());
    }

    @Test
    public void givenValid_whenLeaveVolunteering_thenSuccess(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> createVolunteeringPost = postService.createVolunteeringPost(organizationManagerToken
                ,"Title", "Description",organizationMangerId,volunteeringId);
        int postId = createVolunteeringPost.getData();
        Response<Integer> notificationBefore = userService.getNewUserNotificationsAmount(organizationManagerToken,organizationMangerId);
        int notificationAmountBefore = notificationBefore.getData();
        Response<String> finishVolunteering = volunteeringService.finishVolunteering(aliceToken,aliceId,volunteeringId,"Goodbye");
        Assertions.assertFalse(finishVolunteering.getError());
        Response<Integer> notificationAfter = userService.getNewUserNotificationsAmount(organizationManagerToken,organizationMangerId);
        int notificationAmountAfter = notificationAfter.getData();
        Assertions.assertEquals(notificationAmountBefore+1,notificationAmountAfter);
        Response<List<PastExperience>> getPostPastExperiences = postService.getPostPastExperiences(organizationManagerToken,organizationMangerId,postId);
        Assertions.assertFalse(getPostPastExperiences.getError());
        List<PastExperience> postPastExperiences = getPostPastExperiences.getData();
        Assertions.assertEquals(1,postPastExperiences.size());
        PastExperience experience = postPastExperiences.get(0);
        Assertions.assertEquals(aliceId,experience.getUserId());
        Assertions.assertEquals("Goodbye",experience.getText());
        Response<VolunteeringDTO> getVolunteeringDTO = volunteeringService.getVolunteeringDTO(aliceToken,aliceId,volunteeringId);
        Assertions.assertTrue(getVolunteeringDTO.getError());
        Assertions.assertEquals("User " + aliceId + " has no permission to view " + " volunteering with id " + volunteeringId
                ,getVolunteeringDTO.getErrorString());
        Response<User> getUser = userService.getUserByToken(aliceToken);
        Assertions.assertFalse(getUser.getError());
        List<VolunteeringDTO>  volunteeringsInHistory = getUser.getData().getVolunteeringsInHistory();
        Assertions.assertEquals(1,volunteeringsInHistory.size());
        VolunteeringDTO volunteering = volunteeringsInHistory.get(0);
        Assertions.assertEquals(volunteeringId,volunteering.getId());
    }

    @Test
    public void givenNotVolunteer_whenLeaveVolunteering_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> createVolunteeringPost = postService.createVolunteeringPost(organizationManagerToken
                ,"Title", "Description",organizationMangerId,volunteeringId);
        int postId = createVolunteeringPost.getData();
        Response<Integer> notificationBefore = userService.getNewUserNotificationsAmount(organizationManagerToken,organizationMangerId);
        int notificationAmountBefore = notificationBefore.getData();
        Response<String> finishVolunteering = volunteeringService.finishVolunteering(bobToken,bobId,volunteeringId,"Goodbye");
        Assertions.assertTrue(finishVolunteering.getError());
        Assertions.assertEquals("User " + bobId + " is not a volunteer in volunteering " + volunteeringId,finishVolunteering.getErrorString()) ;
        Response<Integer> notificationAfter = userService.getNewUserNotificationsAmount(organizationManagerToken,organizationMangerId);
        int notificationAmountAfter = notificationAfter.getData();
        Assertions.assertEquals(notificationAmountBefore,notificationAmountAfter);
        Response<User> getUser = userService.getUserByToken(bobToken);
        Assertions.assertFalse(getUser.getError());
        List<VolunteeringDTO>  volunteeringsInHistory = getUser.getData().getVolunteeringsInHistory();
        Assertions.assertEquals(0,volunteeringsInHistory.size());
    }

    @Test
    public void givenValid_whenCreateLocation_thenSuccess(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        Assertions.assertFalse(addVolunteeringLocation.getError());
        Response<List<LocationDTO>> getVolunteeringLocations = volunteeringService.getVolunteeringLocations(organizationManagerToken,
                organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringLocations.getError());
        List<LocationDTO> locations = getVolunteeringLocations.getData();
        Assertions.assertEquals(1,locations.size());
        LocationDTO location = locations.get(0);
        Assertions.assertEquals("Location",location.getName());
    }

    @Test
    public void givenEmptyName_whenCreateLocation_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "", new AddressTuple("City", "Street", "Address"));
        Assertions.assertTrue(addVolunteeringLocation.getError());
        Assertions.assertEquals("Location name cannot be empty", addVolunteeringLocation.getErrorString());
        Response<List<LocationDTO>> getVolunteeringLocations = volunteeringService.getVolunteeringLocations(organizationManagerToken,
                organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringLocations.getError());
        List<LocationDTO> locations = getVolunteeringLocations.getData();
        Assertions.assertEquals(0,locations.size());
    }

    @Test
    public void givenNotManager_whenCreateLocation_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(bobToken,bobId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        Assertions.assertTrue(addVolunteeringLocation.getError());
        Assertions.assertEquals("User " + bobId + " is not a manager in organization " + organizationId, addVolunteeringLocation.getErrorString());
        Response<List<LocationDTO>> getVolunteeringLocations = volunteeringService.getVolunteeringLocations(organizationManagerToken,
                organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringLocations.getError());
        List<LocationDTO> locations = getVolunteeringLocations.getData();
        Assertions.assertEquals(0,locations.size());
    }

    @Test
    public void givenValid_whenCreateRange_thenSuccess(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, 0, locId, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        Assertions.assertFalse(addScheduleRangeToGroup.getError());
        int rangeId = addVolunteeringLocation.getData();
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(1,ranges.size());
        ScheduleRangeDTO range = ranges.get(0);
        Assertions.assertEquals(rangeId,range.getId());
    }

    @Test
    public void givenEndBeforeStart_whenCreateRange_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, 0, locId, 14, 0, 12, 0, -1, -1,
                        null, LocalDate.now());
        Assertions.assertTrue(addScheduleRangeToGroup.getError());
        Assertions.assertEquals("Start time must be before end time",addScheduleRangeToGroup.getErrorString());
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(0,ranges.size());
    }

    @Test
    public void givenGroupOrLocationDontExist_whenCreateRange_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup1 =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, -1, locId, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        Assertions.assertTrue(addScheduleRangeToGroup1.getError());
        Assertions.assertEquals("There is no group with id -1",addScheduleRangeToGroup1.getErrorString());
        Response<Integer> addScheduleRangeToGroup2 =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, 0, -1, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        Assertions.assertTrue(addScheduleRangeToGroup2.getError());
        Assertions.assertEquals("There is no location with id -1",addScheduleRangeToGroup2.getErrorString());
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(0,ranges.size());
    }

    @Test
    public void givenNotManager_whenCreateRange_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(bobToken,bobId,
                        volunteeringId, 0, locId, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        Assertions.assertTrue(addScheduleRangeToGroup.getError());
        Assertions.assertEquals("User " + bobId + " is not a manager in organization " + organizationId,addScheduleRangeToGroup.getErrorString());
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(0,ranges.size());
    }

    @Test
    public void givenValid_whenCreateRestriction_thenSuccess(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, 0, locId, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> addRestrictionToRange = volunteeringService.addRestrictionToRange(organizationManagerToken,organizationMangerId,
                volunteeringId,0,locId,rangeId,12,0,14,0,1);
        Assertions.assertFalse(addRestrictionToRange.getError());
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(1,ranges.size());
        ScheduleRangeDTO range = ranges.get(0);
        List<RestrictionTuple> restrictions = range.getRestrict();
        Assertions.assertEquals(1,restrictions.size());
        RestrictionTuple restriction = restrictions.get(0);
        Assertions.assertEquals(LocalTime.of(12,0),restriction.getStartTime());
        Assertions.assertEquals(LocalTime.of(14,0),restriction.getEndTime());
        Assertions.assertEquals(1,restriction.getAmount());
    }

    @Test
    public void givenInvalid_whenCreateRestriction_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, 0, locId, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> addRestrictionToRange1 = volunteeringService.addRestrictionToRange(organizationManagerToken,organizationMangerId,
                volunteeringId,0,locId,rangeId,12,0,15,0,1);
        Assertions.assertTrue(addRestrictionToRange1.getError());
        Assertions.assertEquals("Restriction times are outside range times",addRestrictionToRange1.getErrorString());
        Response<String> addRestrictionToRange2 = volunteeringService.addRestrictionToRange(organizationManagerToken,organizationMangerId,
                volunteeringId,0,locId,rangeId,14,0,12,0,1);
        Assertions.assertTrue(addRestrictionToRange2.getError());
        Assertions.assertEquals("Restriction start time must be before restriction end time",addRestrictionToRange2.getErrorString());
        Response<String> addRestrictionToRange3 = volunteeringService.addRestrictionToRange(organizationManagerToken,organizationMangerId,
                volunteeringId,0,locId,rangeId,12,0,14,0,-1);
        Assertions.assertTrue(addRestrictionToRange3.getError());
        Assertions.assertEquals("Restriction amount cannot be negative",addRestrictionToRange3.getErrorString());
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(1,ranges.size());
        ScheduleRangeDTO range = ranges.get(0);
        List<RestrictionTuple> restrictions = range.getRestrict();
        Assertions.assertEquals(0,restrictions.size());
    }

    @Test
    public void givenRangeDoesntExist_whenCreateRestriction_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, 0, locId, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> addRestrictionToRange = volunteeringService.addRestrictionToRange(organizationManagerToken,organizationMangerId,
                volunteeringId,0,locId,-1,12,0,14,0,1);
        Assertions.assertTrue(addRestrictionToRange.getError());
        Assertions.assertEquals("No range with Id -1",addRestrictionToRange.getErrorString());
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(1,ranges.size());
        ScheduleRangeDTO range = ranges.get(0);
        List<RestrictionTuple> restrictions = range.getRestrict();
        Assertions.assertEquals(0,restrictions.size());
    }

    @Test
    public void givenRestrictionIntersects_whenCreateRestriction_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, 0, locId, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        volunteeringService.addRestrictionToRange(organizationManagerToken,organizationMangerId,
                volunteeringId,0,locId,rangeId,12,0,13,0,1);
        Response<String> addRestrictionToRange = volunteeringService.addRestrictionToRange(organizationManagerToken,organizationMangerId,
                volunteeringId,0,locId,rangeId,12,0,14,0,1);
        Assertions.assertTrue(addRestrictionToRange.getError());
        Assertions.assertEquals("Cannot add restriction that intersects an existing one",addRestrictionToRange.getErrorString());
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(1,ranges.size());
        ScheduleRangeDTO range = ranges.get(0);
        List<RestrictionTuple> restrictions = range.getRestrict();
        Assertions.assertEquals(1,restrictions.size());
    }

    @Test
    public void givenNotManager_whenCreateRestriction_thenError(){
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken,organizationMangerId,volunteeringId,
                        "Location", new AddressTuple("City", "Street", "Address"));
        int locId = addVolunteeringLocation.getData();
        Response<Integer> addScheduleRangeToGroup =
                volunteeringService.addScheduleRangeToGroup(organizationManagerToken,organizationMangerId,
                        volunteeringId, 0, locId, 12, 0, 14, 0, -1, -1,
                        null, LocalDate.now());
        int rangeId = addScheduleRangeToGroup.getData();
        Response<String> addRestrictionToRange = volunteeringService.addRestrictionToRange(bobToken,bobId,
                volunteeringId,0,locId,rangeId,12,0,14,0,1);
        Assertions.assertTrue(addRestrictionToRange.getError());
        Assertions.assertEquals("User " + bobId + " is not a manager in organization " + organizationId,addRestrictionToRange.getErrorString());
        Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges = volunteeringService.getVolunteeringLocationGroupRanges(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locId);
        Assertions.assertFalse(getVolunteeringLocationGroupRanges.getError());
        List<ScheduleRangeDTO> ranges = getVolunteeringLocationGroupRanges.getData();
        Assertions.assertEquals(1,ranges.size());
        ScheduleRangeDTO range = ranges.get(0);
        List<RestrictionTuple> restrictions = range.getRestrict();
        Assertions.assertEquals(0,restrictions.size());
    }

    @Test
    public void givenLocationExists_whenAssignLocation_thenSuccess(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                        organizationMangerId,volunteeringId,0,locationId,
                        0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(organizationManagerToken,
                        organizationMangerId,aliceId,volunteeringId,locationId);
        Assertions.assertFalse(assignVolunteerToLocation.getError());
        Response<Integer> getUserAssignedLocation =
                volunteeringService.getUserAssignedLocation(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getUserAssignedLocation.getError());
        int assignedLocationId = getUserAssignedLocation.getData();
        Assertions.assertEquals(locationId,assignedLocationId);
    }

    @Test
    public void givenLocationDoesntExist_whenAssignLocation_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locationId,
                0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(organizationManagerToken,
                        organizationMangerId,aliceId,volunteeringId,-1);
        Assertions.assertTrue(assignVolunteerToLocation.getError());
        Assertions.assertEquals("There is no location with id -1", assignVolunteerToLocation.getErrorString());
        Response<Integer> getUserAssignedLocation =
                volunteeringService.getUserAssignedLocation(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getUserAssignedLocation.getError());
        int assignedLocationId = getUserAssignedLocation.getData();
        Assertions.assertEquals(-2,assignedLocationId);
    }

    @Test
    public void givenNotVolunteer_whenAssignLocation_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locationId,
                0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(organizationManagerToken,
                        organizationMangerId,bobId,volunteeringId,locationId);
        Assertions.assertTrue(assignVolunteerToLocation.getError());
        Assertions.assertEquals("User " + bobId + " is not a volunteer in volunteering " + volunteeringId,assignVolunteerToLocation.getErrorString());
    }

    @Test
    public void givenNotManager_whenAssignLocation_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> addVolunteeringLocation =
                volunteeringService.addVolunteeringLocation(organizationManagerToken
                        ,organizationMangerId
                        ,volunteeringId,"Location1", new AddressTuple("City","Street","Address"));
        int locationId = addVolunteeringLocation.getData();
        volunteeringService.addScheduleRangeToGroup(organizationManagerToken,
                organizationMangerId,volunteeringId,0,locationId,
                0,0,23,0,-1,-1,new boolean[]{true,false,false,false,false,false,false},null);
        Response<String> assignVolunteerToLocation =
                volunteeringService.assignVolunteerToLocation(bobToken,
                        bobId,aliceId,volunteeringId,locationId);
        Assertions.assertTrue(assignVolunteerToLocation.getError());
        Assertions.assertEquals("User " + bobId + " cannot assign " + aliceId + " to a location", assignVolunteerToLocation.getErrorString());
        Response<Integer> getUserAssignedLocation =
                volunteeringService.getUserAssignedLocation(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getUserAssignedLocation.getError());
        int assignedLocationId = getUserAssignedLocation.getData();
        Assertions.assertEquals(-2,assignedLocationId);
    }

    @Test
    public void givenManager_whenCreateGroup_thenSuccess(){
        Response<Integer> createNewGroup = volunteeringService.createNewGroup(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(createNewGroup.getError());
        Response<List<Integer>> getVolunteeringGroups = volunteeringService.getVolunteeringGroups(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringGroups.getError());
        List<Integer> groups = getVolunteeringGroups.getData();
        Assertions.assertEquals(2,groups.size());
        Assertions.assertEquals(groups.get(1),createNewGroup.getData());
    }

    @Test
    public void givenNotManager_whenCreateGroup_thenError(){
        Response<Integer> createNewGroup = volunteeringService.createNewGroup(bobToken,bobId,volunteeringId);
        Assertions.assertTrue(createNewGroup.getError());
        Assertions.assertEquals("User " + bobId + " is not a manager in organization " +organizationId,createNewGroup.getErrorString());
        Response<List<Integer>> getVolunteeringGroups = volunteeringService.getVolunteeringGroups(organizationManagerToken,organizationMangerId,volunteeringId);
        Assertions.assertFalse(getVolunteeringGroups.getError());
        List<Integer> groups = getVolunteeringGroups.getData();
        Assertions.assertEquals(1,groups.size());
    }

    @Test
    public void givenGroupExists_whenMoveGroup_thenSuccess(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> createNewGroup = volunteeringService.createNewGroup(organizationManagerToken,organizationMangerId,volunteeringId);
        int groupId = createNewGroup.getData();
        Response<String> moveVolunteerGroup =
                volunteeringService.moveVolunteerGroup(organizationManagerToken,
                        organizationMangerId,aliceId,volunteeringId,groupId);
        Assertions.assertFalse(moveVolunteerGroup.getError());
        Response<Integer> getVolunteerGroup  =
                volunteeringService.getVolunteerGroup(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getVolunteerGroup.getError());
        int userGroupId = getVolunteerGroup.getData();
        Assertions.assertEquals(groupId,userGroupId);
    }

    @Test
    public void givenGroupDoesntExist_whenMoveGroup_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> createNewGroup = volunteeringService.createNewGroup(organizationManagerToken,organizationMangerId,volunteeringId);
        int groupId = createNewGroup.getData();
        Response<String> moveVolunteerGroup =
                volunteeringService.moveVolunteerGroup(organizationManagerToken,
                        organizationMangerId,aliceId,volunteeringId,-1);
        Assertions.assertTrue(moveVolunteerGroup.getError());
        Assertions.assertEquals("There is no group with id -1",moveVolunteerGroup.getErrorString());
        Response<Integer> getVolunteerGroup  =
                volunteeringService.getVolunteerGroup(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getVolunteerGroup.getError());
        int userGroupId = getVolunteerGroup.getData();
        Assertions.assertEquals(0,userGroupId);
    }

    @Test
    public void givenNotVolunteer_whenMoveGroup_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> createNewGroup = volunteeringService.createNewGroup(organizationManagerToken,organizationMangerId,volunteeringId);
        int groupId = createNewGroup.getData();
        Response<String> moveVolunteerGroup =
                volunteeringService.moveVolunteerGroup(organizationManagerToken,
                        organizationMangerId,bobId,volunteeringId,groupId);
        Assertions.assertTrue(moveVolunteerGroup.getError());
        Assertions.assertEquals("User " + bobId + " is not a volunteer in volunteering " +volunteeringId,moveVolunteerGroup.getErrorString());
        Response<Integer> getVolunteerGroup  =
                volunteeringService.getVolunteerGroup(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getVolunteerGroup.getError());
        int userGroupId = getVolunteerGroup.getData();
        Assertions.assertEquals(0,userGroupId);
    }

    @Test
    public void givenNotManager_whenMoveGroup_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> createNewGroup = volunteeringService.createNewGroup(organizationManagerToken,organizationMangerId,volunteeringId);
        int groupId = createNewGroup.getData();
        Response<String> moveVolunteerGroup =
                volunteeringService.moveVolunteerGroup(bobToken,
                        bobId,aliceId,volunteeringId,groupId);
        Assertions.assertTrue(moveVolunteerGroup.getError());
        Assertions.assertEquals("User " + bobId + " is not a manager in organization " +organizationId,moveVolunteerGroup.getErrorString());
        Response<Integer> getVolunteerGroup  =
                volunteeringService.getVolunteerGroup(aliceToken,aliceId,volunteeringId);
        Assertions.assertFalse(getVolunteerGroup.getError());
        int userGroupId = getVolunteerGroup.getData();
        Assertions.assertEquals(0,userGroupId);
    }

    @Test
    public void givenManager_whenNotifyVolunteers_thenSendNotification(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> notificationBeforeAlice = userService.getNewUserNotificationsAmount(aliceToken,aliceId);
        int notificationAmountBeforeAlice = notificationBeforeAlice.getData();
        Response<Integer> notificationBeforeBob = userService.getNewUserNotificationsAmount(bobToken,bobId);
        int notificationAmountBeforeBob = notificationBeforeBob.getData();
        Response<String> sendUpdatesToVolunteers = volunteeringService
                .sendUpdatesToVolunteers(organizationManagerToken,organizationMangerId,volunteeringId,"Hello!");
        Assertions.assertFalse(sendUpdatesToVolunteers.getError());
        Response<Integer> notificationAfterAlice = userService.getNewUserNotificationsAmount(aliceToken,aliceId);
        int notificationAmountAfterAlice = notificationAfterAlice.getData();
        Response<Integer> notificationAfterBob = userService.getNewUserNotificationsAmount(bobToken,bobId);
        int notificationAmountAfterBob = notificationAfterBob.getData();

        Assertions.assertEquals(notificationAmountBeforeAlice+1,notificationAmountAfterAlice);
        Assertions.assertEquals(notificationAmountBeforeBob,notificationAmountAfterBob);

        Response<List<Notification>> getUserNotifications = userService.getUserNotifications(aliceToken,aliceId);
        Assertions.assertFalse(getUserNotifications.getError());
        List<Notification> notifications = getUserNotifications.getData();
        notifications = notifications.stream().sorted((a,b) -> a.getTimestamp().after(b.getTimestamp()) ? -1 : a.getTimestamp().before(b.getTimestamp()) ? 1 : 0).toList();
        Notification notification = notifications.get(0);
        Assertions.assertEquals("Update from Volunteering: Hello!",notification.getMessage());
    }

    @Test
    public void givenNotManager_whenNotifyVolunteers_thenError(){
        joinUserToVolunteering(aliceToken,aliceId);
        Response<Integer> notificationBeforeAlice = userService.getNewUserNotificationsAmount(aliceToken,aliceId);
        int notificationAmountBeforeAlice = notificationBeforeAlice.getData();
        Response<Integer> notificationBeforeBob = userService.getNewUserNotificationsAmount(bobToken,bobId);
        int notificationAmountBeforeBob = notificationBeforeBob.getData();
        Response<String> sendUpdatesToVolunteers = volunteeringService
                .sendUpdatesToVolunteers(bobToken,bobId,volunteeringId,"Hello!");
        Assertions.assertTrue(sendUpdatesToVolunteers.getError());
        Assertions.assertEquals("User " + bobId + " is not a manager in organization " + organizationId + " of volunteering " + volunteeringId,sendUpdatesToVolunteers.getErrorString());
        Response<Integer> notificationAfterAlice = userService.getNewUserNotificationsAmount(aliceToken,aliceId);
        int notificationAmountAfterAlice = notificationAfterAlice.getData();
        Response<Integer> notificationAfterBob = userService.getNewUserNotificationsAmount(bobToken,bobId);
        int notificationAmountAfterBob = notificationAfterBob.getData();

        Assertions.assertEquals(notificationAmountBeforeAlice,notificationAmountAfterAlice);
        Assertions.assertEquals(notificationAmountBeforeBob,notificationAmountAfterBob);
    }

    @Test
    public void givenBguStudent_whenExportFormattedPdf_thenExportPdf() throws DocumentException, IOException {
        joinUserToVolunteering(bguStudentToken,bguStudentId);
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(14,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringService
                .requestHoursApproval(bguStudentToken,bguStudentId,volunteeringId,startTime,endTime);
        volunteeringService
                .approveUserHours(organizationManagerToken,organizationMangerId,volunteeringId,bguStudentId,startTime,endTime);
        Response<String> getUserApprovedHoursFormatted =
                volunteeringService.getUserApprovedHoursFormatted(bguStudentToken,bguStudentId,volunteeringId,"123456789");
        Assertions.assertFalse(getUserApprovedHoursFormatted.getError());
        String fileLocation = getUserApprovedHoursFormatted.getData();
        File file = new File(fileLocation);
        File parentDir =  file.getParentFile();
        file.delete();
        if(parentDir.isDirectory() && parentDir.list().length == 0) {
            parentDir.delete();
        }
    }

    @Test
    public void givenTauStudent_whenExportFormattedPdf_thenError() throws DocumentException, IOException {
        joinUserToVolunteering(tauStudentToken,tauStudentId);
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(14,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringService
                .requestHoursApproval(tauStudentToken,tauStudentId,volunteeringId,startTime,endTime);
        volunteeringService
                .approveUserHours(organizationManagerToken,organizationMangerId,volunteeringId,tauStudentId,startTime,endTime);
        Response<String> getUserApprovedHoursFormatted =
                volunteeringService.getUserApprovedHoursFormatted(tauStudentToken,tauStudentId,volunteeringId,"123456789");
        Assertions.assertTrue(getUserApprovedHoursFormatted.getError());
        Assertions.assertEquals("University not supported",getUserApprovedHoursFormatted.getErrorString());
    }

    @Test
    public void givenNotStudent_whenExportFormattedPdf_thenError() throws DocumentException, IOException {
        joinUserToVolunteering(aliceToken,aliceId);
        LocalDate today = LocalDate.now();
        Date startTime = Date.from(LocalTime.of(12,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(LocalTime.of(14,0).atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringService
                .requestHoursApproval(aliceToken,aliceId,volunteeringId,startTime,endTime);
        volunteeringService
                .approveUserHours(organizationManagerToken,organizationMangerId,volunteeringId,aliceId,startTime,endTime);
        Response<String> getUserApprovedHoursFormatted =
                volunteeringService.getUserApprovedHoursFormatted(aliceToken,aliceId,volunteeringId,"123456789");
        Assertions.assertTrue(getUserApprovedHoursFormatted.getError());
        Assertions.assertEquals("User " + aliceId + " is not a student",getUserApprovedHoursFormatted.getErrorString());
    }
}
