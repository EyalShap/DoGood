package com.dogood.dogoodbackend.domain.volunteering.scheduling;

import com.dogood.dogoodbackend.domain.volunteerings.ScheduleRange;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.*;
import com.dogood.dogoodbackend.jparepos.AppointmentJPA;
import com.dogood.dogoodbackend.jparepos.HourRequestJPA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SchedulingFacadeIntegrationTests {
    private SchedulingFacade schedulingFacade;
    private SchedulingManager schedulingManager;

    @Autowired
    HourRequestJPA hourRequestJPA;
    @Autowired
    AppointmentJPA appointmentJPA;

    @BeforeEach
    public void setUp() {
        hourRequestJPA.deleteAll();
        appointmentJPA.deleteAll();
        schedulingManager = new DatabaseSchedulingManager(hourRequestJPA, appointmentJPA);
        schedulingFacade = new SchedulingFacade(schedulingManager);
    }

    @Test
    public void givenValid_whenMakeAppointment_thenMakeAppointment(){
        ScheduleRange scheduleRange = new ScheduleRange(0,0,
                LocalTime.of(0,0), LocalTime.of(23,59),-1,-1,null,
                LocalDate.now());
        schedulingFacade.makeAppointment("User",0,scheduleRange,LocalTime.of(12,0), LocalTime.of(14,0),null, LocalDate.now());
        Assertions.assertEquals(1,schedulingFacade.getUserAppointments("User",0).size());
        ScheduleAppointmentDTO appointment = schedulingFacade.getUserAppointments("User",0).get(0);
        Assertions.assertEquals(LocalTime.of(12,0), appointment.getStartTime());
        Assertions.assertEquals(LocalTime.of(14,0), appointment.getEndTime());
    }

    @Test
    public void givenIntersectsPrevious_whenMakeAppointment_thenThrowException(){
        ScheduleRange scheduleRange = new ScheduleRange(0,0,
                LocalTime.of(0,0), LocalTime.of(23,59),-1,-1,null,
                LocalDate.now());
        schedulingFacade.makeAppointment("User",0,scheduleRange,LocalTime.of(12,0), LocalTime.of(14,0),null, LocalDate.now());
        Assertions.assertThrows(IllegalArgumentException.class,() -> schedulingFacade.makeAppointment("User",0,scheduleRange,LocalTime.of(12,0), LocalTime.of(14,0),null, LocalDate.now()));
        Assertions.assertEquals(1,schedulingFacade.getUserAppointments("User",0).size());
    }

    @Test
    public void givenRangeIsFull_whenMakeAppointment_thenThrowException(){
        ScheduleRange scheduleRange = new ScheduleRange(0,0,
                LocalTime.of(0,0), LocalTime.of(23,59),-1,-1,null,
                LocalDate.now());
        scheduleRange.addRestriction(new RestrictionTuple(LocalTime.of(12,0), LocalTime.of(13,0),0));
        Assertions.assertThrows(UnsupportedOperationException.class,() -> schedulingFacade.makeAppointment("User",0,scheduleRange,LocalTime.of(12,0), LocalTime.of(14,0),null, LocalDate.now()));
        Assertions.assertEquals(0,schedulingFacade.getUserAppointments("User",0).size());
    }

    @Test
    public void givenValid_whenAddHourApprovalRequest_thenAddRequest(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        schedulingFacade.addHourApprovalRequest("User",0,startDate,endDate);
        Assertions.assertEquals(1,schedulingFacade.getHourApprovalRequests(0).size());
        HourApprovalRequest request = schedulingFacade.getHourApprovalRequests(0).get(0);
        Assertions.assertEquals(startDate.getTime(), request.getStartTime().getTime());
        Assertions.assertEquals(endDate.getTime(), request.getEndTime().getTime());
    }

    @Test
    public void givenIntersectsPrevious_whenAddHourApprovalRequest_thenThrowException(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        schedulingFacade.addHourApprovalRequest("User",0,startDate,endDate);
        Assertions.assertThrows(UnsupportedOperationException.class,() -> schedulingFacade.addHourApprovalRequest("User",0,startDate,endDate));
        Assertions.assertEquals(1,schedulingFacade.getHourApprovalRequests(0).size());
    }

    @Test
    public void givenAlreadyApproved_whenAddHourApprovalRequest_thenThrowException(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        schedulingFacade.addHourApprovalRequest("User",0,startDate,endDate);
        schedulingFacade.approveUserHours("User",0,startDate,endDate);
        Assertions.assertThrows(UnsupportedOperationException.class,() -> schedulingFacade.addHourApprovalRequest("User",0,startDate,endDate));
        Assertions.assertEquals(0,schedulingFacade.getHourApprovalRequests(0).size());
    }

    @Test
    public void givenValid_whenApproveUserHours_thenApproveRequest(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        schedulingFacade.addHourApprovalRequest("User",0,startDate,endDate);
        schedulingFacade.approveUserHours("User",0,startDate,endDate);
        Assertions.assertEquals(1,schedulingFacade.getUserApprovedHours("User", List.of(0)).size());
        HourApprovalRequest approved = schedulingFacade.getUserApprovedHours("User", List.of(0)).get(0);
        Assertions.assertEquals(startDate.getTime(), approved.getStartTime().getTime());
        Assertions.assertEquals(endDate.getTime(), approved.getEndTime().getTime());
    }

    @Test
    public void givenAlreadyApproved_whenApproveUserHours_thenThrowException(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        schedulingFacade.addHourApprovalRequest("User",0,startDate,endDate);
        schedulingFacade.approveUserHours("User",0,startDate,endDate);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> schedulingFacade.approveUserHours("User",0,startDate,endDate));
        Assertions.assertEquals(1,schedulingFacade.getUserApprovedHours("User", List.of(0)).size());
    }

    @Test
    public void givenNotRequested_whenApproveUserHours_thenThrowException(){
        LocalDate now = LocalDate.now();
        LocalTime startTime = LocalTime.of(12,0);
        LocalTime endTime = LocalTime.of(14,0);
        Date startDate = Date.from(now.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> schedulingFacade.approveUserHours("User",0,startDate,endDate));
        Assertions.assertEquals(0,schedulingFacade.getUserApprovedHours("User", List.of(0)).size());
    }
}
