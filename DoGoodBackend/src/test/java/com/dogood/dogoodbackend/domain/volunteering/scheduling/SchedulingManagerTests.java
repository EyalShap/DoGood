package com.dogood.dogoodbackend.domain.volunteering.scheduling;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.*;
import com.dogood.dogoodbackend.jparepos.AppointmentJPA;
import com.dogood.dogoodbackend.jparepos.HourRequestJPA;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SchedulingManagerTests {
    @Autowired
    private AppointmentJPA appointmentJPA;
    @Autowired
    private HourRequestJPA hourRequestJPA;

    private SchedulingManager memoryManager;
    private SchedulingManager databaseManager;


    Stream<SchedulingManager> managers() {
        return Stream.of(memoryManager, databaseManager);
    }

    @BeforeAll
    public void createManagers(){
        memoryManager = new MemorySchedulingManager();
        databaseManager = new DatabaseSchedulingManager(hourRequestJPA, appointmentJPA);
    }

    @BeforeEach
    public void setUp() {
        appointmentJPA.deleteAll();
        hourRequestJPA.deleteAll();
        memoryManager = new MemorySchedulingManager();
    }

    @ParameterizedTest
    @MethodSource("managers")
    public void givenWeekDays_whenGetAmountOfAppointments_returnAmountOfAppointments(SchedulingManager schedulingManager) {
        boolean[] weekDays1 = new boolean[7];
        boolean[] weekDays2 = new boolean[7];
        weekDays1[0] = true;
        weekDays1[1] = true;
        weekDays2[2] = true;
        ScheduleAppointment appointment1 = new ScheduleAppointment("Jim", 0, 0, LocalTime.of(12,0), LocalTime.of(14,0));
        ScheduleAppointment appointment2 = new ScheduleAppointment("Bim", 0, 0, LocalTime.of(12,0), LocalTime.of(14,0));
        ScheduleAppointment appointment3 = new ScheduleAppointment("Dim", 0, 0, LocalTime.of(10,0), LocalTime.of(12,0));
        ScheduleAppointment appointment4 = new ScheduleAppointment("Nim", 0, 0, LocalTime.of(12,0), LocalTime.of(14,0));

        appointment1.setWeekDays(weekDays1);
        appointment2.setWeekDays(weekDays1);
        appointment3.setWeekDays(weekDays1);
        appointment4.setWeekDays(weekDays2);
        schedulingManager.makeAppointment(appointment1);
        schedulingManager.makeAppointment(appointment2);
        schedulingManager.makeAppointment(appointment3);
        schedulingManager.makeAppointment(appointment4);

        RestrictionTuple r = new RestrictionTuple(LocalTime.of(12,0), LocalTime.of(14,0),2);

        Assertions.assertEquals(2, schedulingManager.getAmountOfAppointmentsInRestrict(0,0,r,weekDays1,null));
    }

    @ParameterizedTest
    @MethodSource("managers")
    public void givenOneTime_whenGetAmountOfAppointments_returnAmountOfAppointments(SchedulingManager schedulingManager) {
        LocalDate date1 = LocalDate.of(2025,1,1);
        LocalDate date2 = LocalDate.of(2025,1,2);
        ScheduleAppointment appointment1 = new ScheduleAppointment("Jim", 0, 0, LocalTime.of(12,0), LocalTime.of(14,0));
        ScheduleAppointment appointment2 = new ScheduleAppointment("Bim", 0, 0, LocalTime.of(12,0), LocalTime.of(14,0));
        ScheduleAppointment appointment3 = new ScheduleAppointment("Dim", 0, 0, LocalTime.of(10,0), LocalTime.of(12,0));
        ScheduleAppointment appointment4 = new ScheduleAppointment("Nim", 0, 0, LocalTime.of(12,0), LocalTime.of(14,0));

        appointment1.setOneTime(date1);
        appointment2.setOneTime(date1);
        appointment3.setOneTime(date1);
        appointment4.setOneTime(date2);
        schedulingManager.makeAppointment(appointment1);
        schedulingManager.makeAppointment(appointment2);
        schedulingManager.makeAppointment(appointment3);
        schedulingManager.makeAppointment(appointment4);

        RestrictionTuple r = new RestrictionTuple(LocalTime.of(12,0), LocalTime.of(14,0),2);

        Assertions.assertEquals(2, schedulingManager.getAmountOfAppointmentsInRestrict(0,0,r,null,date1));
    }
}
