package com.dogood.dogoodbackend.domain.volunteering;

import com.dogood.dogoodbackend.domain.externalAIAPI.Gemini;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.users.notificiations.PushNotificationSender;
import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingFacade;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingManager;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.FacadeManager;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class VolunteeringFacadeIntegrationTest {

    @Autowired
    private VolunteeringJPA volunteeringJPA;
    @Autowired
    private HourRequestJPA hourRequestJPA;
    @Autowired
    private AppointmentJPA appointmentJPA;
    @Autowired
    private OrganizationJPA organizationJPA;
    @Autowired
    private UserJPA userJPA;
    @Autowired
    private NotificationJPA notificationJPA;

    @MockitoBean
    private Gemini gemini;
    @MockitoBean
    private PushNotificationSender pushNotificationSender;
    @MockitoBean
    private NotificationSocketSender notificationSocketSender;

    @Autowired
    private FacadeManager facadeManager;

    private UsersFacade usersFacade;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;

    private String username;
    private int orgId;
    private int volId;
    private String volunteer;

    @BeforeEach
    public void setUp() {
        volunteeringJPA.deleteAll();
        hourRequestJPA.deleteAll();
        appointmentJPA.deleteAll();
        organizationJPA.deleteAll();
        userJPA.deleteAll();
        notificationJPA.deleteAll();

        facadeManager.getNotificationSystem().setPushNotificationSender(pushNotificationSender);
        facadeManager.getNotificationSystem().setSender(notificationSocketSender);

        usersFacade = facadeManager.getUsersFacade();
        volunteeringFacade = facadeManager.getVolunteeringFacade();
        organizationsFacade = facadeManager.getOrganizationsFacade();
        username = "User";
        volunteer = "Volunteer";
        usersFacade.register(username, "123456", "Name", "email@email.com","052-0520520",new Date());
        usersFacade.register(volunteer, "123456", "Name", "email@email.com","052-0520520",new Date());
        orgId = organizationsFacade.createOrganization("Organization", "Description", "052-0520520","email@email.com",username);
        volId = organizationsFacade.createVolunteering(orgId,"Volunteering","Description",username);
        volunteeringFacade.requestToJoinVolunteering(volunteer,volId,"");
        volunteeringFacade.acceptUserJoinRequest(username,volId,volunteer,0);
    }
    @Test
    public void givenValid_whenCreateVolunteering_thenCreate(){
        int newVolId = volunteeringFacade.createVolunteering(username,orgId,"Volunteering2","Description");
        Assertions.assertDoesNotThrow(() -> volunteeringFacade.getVolunteeringDTO(newVolId));
        VolunteeringDTO vol = volunteeringFacade.getVolunteeringDTO(newVolId);
        Assertions.assertEquals("Volunteering2",vol.getName());
        Assertions.assertEquals("Description",vol.getDescription());
    }

    @Test
    public void givenValidCode_whenScanCode_thenCreateHourRequest(){
        LocalTime start = LocalTime.now().minusHours(1);
        LocalTime end = LocalTime.now().plusHours(1);
        if(LocalTime.now().getHour() == 0){
            start = LocalTime.now();
        }
        if(LocalTime.now().getHour() == 23){
            end = LocalTime.now();
        }
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        int rangeId = volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        volunteeringFacade.makeAppointment(volunteer,volId,0,-1,rangeId,
                start,end,null,LocalDate.now());
        volunteeringFacade.updateVolunteeringScanDetails(username,volId, ScanTypes.ONE_SCAN, ApprovalType.MANUAL);
        String code = volunteeringFacade.makeVolunteeringCode(username,volId,false);
        Assertions.assertDoesNotThrow(()->volunteeringFacade.scanCode(volunteer,code));
        Assertions.assertEquals(1, volunteeringFacade.getVolunteeringHourRequests(username,volId).size());
    }

    @Test
    public void givenInvalidCode_whenScanCode_thenThrowException(){
        LocalTime start = LocalTime.now().minusHours(1);
        LocalTime end = LocalTime.now().plusHours(1);
        if(LocalTime.now().getHour() == 0){
            start = LocalTime.now();
        }
        if(LocalTime.now().getHour() == 23){
            end = LocalTime.now();
        }
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        int rangeId = volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        volunteeringFacade.makeAppointment(volunteer,volId,0,-1,rangeId,
                start,end,null,LocalDate.now());
        volunteeringFacade.updateVolunteeringScanDetails(username,volId, ScanTypes.ONE_SCAN, ApprovalType.MANUAL);
        String code = volunteeringFacade.makeVolunteeringCode(username,volId,false);
        Assertions.assertThrows(IllegalArgumentException.class, ()->volunteeringFacade.scanCode(volunteer,"invalid"));
    }

    @Test
    public void givenNoAppointment_whenScanCode_thenThrowException(){
        LocalTime start = LocalTime.now().minusHours(1);
        LocalTime end = LocalTime.now().plusHours(1);
        if(LocalTime.now().getHour() == 0){
            start = LocalTime.now();
        }
        if(LocalTime.now().getHour() == 23){
            end = LocalTime.now();
        }
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        int rangeId = volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        volunteeringFacade.updateVolunteeringScanDetails(username,volId, ScanTypes.ONE_SCAN, ApprovalType.MANUAL);
        String code = volunteeringFacade.makeVolunteeringCode(username,volId,false);
        Assertions.assertThrows(IllegalArgumentException.class, ()->volunteeringFacade.scanCode(volunteer,code));
    }

    @Test
    public void givenVolunteeringExists_whenMakeCode_thenReturnCode(){
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        volunteeringFacade.updateVolunteeringScanDetails(username,volId, ScanTypes.ONE_SCAN, ApprovalType.MANUAL);
        String code = volunteeringFacade.makeVolunteeringCode(username,volId,false);
        String[] splits = code.split(":");
        Assertions.assertEquals(volId,Integer.parseInt(splits[0]));
    }

    @Test
    public void givenVolunteeringDoesNotExist_whenMakeCode_thenThrowException(){
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        volunteeringFacade.updateVolunteeringScanDetails(username,volId, ScanTypes.ONE_SCAN, ApprovalType.MANUAL);
        Assertions.assertThrows(IllegalArgumentException.class, () -> volunteeringFacade.makeVolunteeringCode(username,-1,false));
    }

    @Test
    public void givenValid_whenRequestToJoinVolunteering_thenRequestToJoin(){
        String another = "Another";
        usersFacade.register(another, "123456", "Name", "email@email.com","052-0520520",new Date());
        Assertions.assertDoesNotThrow(() ->volunteeringFacade.requestToJoinVolunteering(another,volId,""));
        Assertions.assertEquals(1, volunteeringFacade.getVolunteeringJoinRequests(username,volId).size());
    }

    @Test
    public void givenVolunteeringDoesNotExist_whenRequestToJoinVolunteering_thenThrowException(){
        String another = "Another";
        usersFacade.register(another, "123456", "Name", "email@email.com","052-0520520",new Date());
        Assertions.assertThrows(IllegalArgumentException.class, () ->volunteeringFacade.requestToJoinVolunteering(another,-1,""));
    }

    @Test
    public void givenValid_whenAcceptJoinRequest_thenAddUserToVolunteering(){
        String another = "Another";
        usersFacade.register(another, "123456", "Name", "email@email.com","052-0520520",new Date());
        volunteeringFacade.requestToJoinVolunteering(another,volId,"");
        Assertions.assertDoesNotThrow(() -> volunteeringFacade.acceptUserJoinRequest(username,volId,another,0));
        Map<String,Integer> volunteers = volunteeringFacade.getVolunteeringVolunteers(volId);
        Assertions.assertEquals(2, volunteers.size());
        Assertions.assertTrue(volunteers.containsKey(another));
        Assertions.assertEquals(0, volunteers.get(another));
    }

    @Test
    public void givenVolunteeringDoesNotExist_whenAcceptJoinRequest_thenThrowException(){
        String another = "Another";
        usersFacade.register(another, "123456", "Name", "email@email.com","052-0520520",new Date());
        volunteeringFacade.requestToJoinVolunteering(another,volId,"");
        Assertions.assertThrows(IllegalArgumentException.class, () ->volunteeringFacade.acceptUserJoinRequest(username,-1,another,0));
    }

    @Test
    public void givenValid_whenFinishVolunteering_thenLeaveVolunteering(){
        Assertions.assertDoesNotThrow(()->volunteeringFacade.finishVolunteering(volunteer,volId,"Bye"));
        User user = usersFacade.getUser(volunteer);
        Assertions.assertEquals(1, user.getVolunteeringsInHistory().size());
        VolunteeringDTO volunteeringDTO = user.getVolunteeringsInHistory().get(0);
        Assertions.assertEquals(volId,volunteeringDTO.getId());
        Assertions.assertEquals(0,volunteeringFacade.getVolunteeringVolunteers(volId).size());
        Assertions.assertEquals(1,volunteeringFacade.getVolunteeringPastExperiences(volId).size());
        PastExperience experience = volunteeringFacade.getVolunteeringPastExperiences(volId).get(0);
        Assertions.assertEquals(volunteer,experience.getUserId());
    }

    @Test
    public void givenVolunteeringDoesNotExist_whenFinishVolunteering_thenThrowException(){
        Assertions.assertThrows(IllegalArgumentException.class,()->volunteeringFacade.finishVolunteering(volunteer,-1,"Bye"));
    }

    @Test
    public void givenValid_whenMakeAppointment_thenMakeAppointment(){
        LocalTime start = LocalTime.now().getHour() == 0 ? LocalTime.now() : LocalTime.now().minusHours(1);
        LocalTime end = LocalTime.now().getHour() == 23 ? LocalTime.now() : LocalTime.now().plusHours(1);
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        int rangeId = volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        Assertions.assertDoesNotThrow(() -> volunteeringFacade.makeAppointment(volunteer,volId,0,-1,rangeId,
                start,end,null,LocalDate.now()));
        Assertions.assertEquals(1, volunteeringFacade.getVolunteerAppointments(volunteer,volId).size());
    }

    @Test
    public void givenVolunteeringDoesntExist_whenMakeAppointment_thenThrowException(){
        LocalTime start = LocalTime.now().getHour() == 0 ? LocalTime.now() : LocalTime.now().minusHours(1);
        LocalTime end = LocalTime.now().getHour() == 23 ? LocalTime.now() : LocalTime.now().plusHours(1);
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        int rangeId = volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        Assertions.assertThrows(IllegalArgumentException.class, () -> volunteeringFacade.makeAppointment(volunteer,-1,0,-1,rangeId,
                start,end,null,LocalDate.now()));
        Assertions.assertEquals(0, volunteeringFacade.getVolunteerAppointments(volunteer,volId).size());
    }

    @Test
    public void givenValid_whenRequestHourApproval_thenRequestHourApproval(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        Assertions.assertDoesNotThrow(() -> volunteeringFacade.requestHoursApproval(volunteer,volId,
                startDate,endDate));
        Assertions.assertEquals(1, volunteeringFacade.getVolunteeringHourRequests(username,volId).size());
    }

    @Test
    public void givenVolunteeringDoesntExist_whenRequestHourApproval_thenThrowException(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        Assertions.assertThrows(IllegalArgumentException.class,() -> volunteeringFacade.requestHoursApproval(volunteer,-1,
                startDate,endDate));
        Assertions.assertEquals(0, volunteeringFacade.getVolunteeringHourRequests(username,volId).size());
    }

    @Test
    public void givenValid_whenApproveHours_thenApproveHours(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        volunteeringFacade.requestHoursApproval(volunteer,volId,startDate,endDate);
        Assertions.assertDoesNotThrow(() -> volunteeringFacade.approveUserHours(username,volId,volunteer,
                startDate,endDate));
        Assertions.assertEquals(1, volunteeringFacade.getUserApprovedHours(volunteer,List.of(volId)).size());
    }

    @Test
    public void givenVolunteeringDoesntExist_whenApproveHours_thenThrowException(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        volunteeringFacade.requestHoursApproval(volunteer,volId, startDate,endDate);
        Assertions.assertThrows(IllegalArgumentException.class,() -> volunteeringFacade.approveUserHours(username,-1,volunteer,
                startDate,endDate));
        Assertions.assertEquals(0, volunteeringFacade.getUserApprovedHours(username,List.of(volId)).size());
    }

    @Test
    public void givenNotManager_whenApproveHours_thenThrowException(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        volunteeringFacade.disableVolunteeringLocations(username,volId);
        volunteeringFacade.addScheduleRangeToGroup
                (username,volId,0,-1, LocalTime.of(0,0),LocalTime.of(23,59),-1,-1,
                        null, LocalDate.now());
        volunteeringFacade.requestHoursApproval(volunteer,volId, startDate,endDate);
        Assertions.assertThrows(IllegalArgumentException.class,() -> volunteeringFacade.approveUserHours(volunteer,volId,volunteer,
                startDate,endDate));
        Assertions.assertEquals(0, volunteeringFacade.getUserApprovedHours(username,List.of(volId)).size());
    }
}


