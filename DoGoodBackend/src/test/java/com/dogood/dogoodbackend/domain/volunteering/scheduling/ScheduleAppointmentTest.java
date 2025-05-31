package com.dogood.dogoodbackend.domain.volunteering.scheduling;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScheduleAppointmentTest {

    private ScheduleAppointment appointment;
    private boolean[] weekDays;
    @BeforeEach
    void setUp() {
        this.weekDays = new boolean[]{true, false, false, false, false, false, false};
        appointment = new ScheduleAppointment("user1", 100, 1, LocalTime.of(9, 0), LocalTime.of(12, 0), null,weekDays );
    }

    @Test
    void whenConstructed_givenAllDays_thenWeekDaysIs127() {
        ScheduleAppointment appointment1 = new ScheduleAppointment("user1", 100, 1, LocalTime.of(9, 0), LocalTime.of(12, 0), null, new boolean[]{true, true, true, true, true, true, true});
        assertEquals(127, appointment1.getWeekDays());
    }

    @Test
    void whenConstructed_givenNullBoth_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new ScheduleAppointment("user1", 100, 1, LocalTime.of(9, 0), LocalTime.of(12, 0), null, null));
    }

    @Test
    void whenConstructed_givenNullWeekDays_thenWeekDaysIsNegative() {
        ScheduleAppointment appointment1 = new ScheduleAppointment("user1", 100, 1, LocalTime.of(9, 0), LocalTime.of(12, 0), LocalDate.of(2024,1,1),  null);
        assertTrue(appointment1.getWeekDays()<0);
    }

    @Test
    void whenIncludesDate_givenMatchingDate_thenReturnsTrue() {
        Date date = Date.from(YearMonth.of(2025, Month.FEBRUARY).atEndOfMonth().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.of(10,0)).atZone(ZoneId.systemDefault()).toInstant());
        assertTrue(appointment.includesDate(date, 60));
    }

    @Test
    void whenIncludesDate_givenNonMatchingDate_thenReturnsFalse() {
        Date date = new Date(System.currentTimeMillis() + 86400000L);
        assertFalse(appointment.includesDate(date, 60));
    }

    @Test
    void whenMatchRange_givenMatchingRange_thenReturnsTrue() {
        Date start = Date.from(YearMonth.of(2025, Month.FEBRUARY).atEndOfMonth().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.of(8,46)).atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(YearMonth.of(2025, Month.FEBRUARY).atEndOfMonth().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.of(12,14)).atZone(ZoneId.systemDefault()).toInstant());
        assertTrue(appointment.matchRange(start, end, 15));
    }

    @Test
    void whenMatchRange_givenNonMatchingRange_thenReturnsFalse() {
        Date start = new Date(System.currentTimeMillis() + 86400000L);
        Date end = new Date(System.currentTimeMillis() + 90000000L);
        assertFalse(appointment.matchRange(start, end, 60));
    }

    @Test
    void whenGetDefiniteRange_givenMatchingDate_thenReturnsRange() {
        LocalDate day = YearMonth.of(2025, Month.FEBRUARY).atEndOfMonth().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        assertDoesNotThrow(() -> appointment.getDefiniteRange(day));
    }

    @Test
    void whenGetDefiniteRange_givenNonMatchingDate_thenException() {
        int dayOfAppointment = -1;
        for(int i = 0; i < 7; i++){
            if(weekDays[i]){
                dayOfAppointment = i;
                break;
            }
        }
        LocalDate now = LocalDate.now();
        LocalDate day = now.plusDays(now.getDayOfWeek().getValue()%7 == dayOfAppointment ? 1 : 0);
        assertThrows(UnsupportedOperationException.class, () -> appointment.getDefiniteRange(day));
    }

    @Test
    void whenIntersect_givenOverlappingAppointments_thenReturnsTrue() {
        ScheduleAppointment other = new ScheduleAppointment("user2", 100, 1, LocalTime.of(10, 0), LocalTime.of(11, 0), null, new boolean[]{true, false, false, false, false, false, false});
        assertTrue(appointment.intersect(other));
    }

    @Test
    void whenIntersect_givenNonOverlappingAppointments_thenReturnsFalse() {
        ScheduleAppointment other = new ScheduleAppointment("user2", 100, 1, LocalTime.of(13, 0), LocalTime.of(14, 0), null, new boolean[]{true, false, false, false, false, false, false});
        assertFalse(appointment.intersect(other));
    }

    @Test
    void whenIntersect_givenOverlappingHourOtherDay_thenReturnsFalse() {
        ScheduleAppointment other = new ScheduleAppointment("user2", 100, 1, LocalTime.of(10, 0), LocalTime.of(11, 0), null, new boolean[]{false, true, false, false, false, false, false});
        assertFalse(appointment.intersect(other));
    }
}
