package com.dogood.dogoodbackend.domain.volunteering;

import com.dogood.dogoodbackend.domain.volunteerings.ScheduleRange;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

class ScheduleRangeTest {

    private ScheduleRange scheduleRange;

    @BeforeEach
    void setUp() {
        scheduleRange = new ScheduleRange(1, 100, LocalTime.of(9, 0), LocalTime.of(17, 0), 30, 120, new boolean[]{true, true, true, true, true, false, false}, null);
    }

    @Test
    void whenConstructed_givenAllDays_thenWeekDaysIs127() {
        ScheduleRange scheduleRange = new ScheduleRange(1, 100, LocalTime.of(9, 0), LocalTime.of(17, 0), 30, 120, new boolean[]{true, true, true, true, true, true, true}, null);
        assertEquals(127, scheduleRange.getWeekDays());
    }

    @Test
    void whenConstructed_givenNullBoth_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new ScheduleRange(1, 100, LocalTime.of(9, 0), LocalTime.of(17, 0), 30, 120, null, null));
    }

    @Test
    void whenConstructed_givenNullWeekDays_thenWeekDaysIsNegative() {
        ScheduleRange scheduleRange = new ScheduleRange(1, 100, LocalTime.of(9, 0), LocalTime.of(17, 0), 30, 120, null, LocalDate.of(2024,1,1));
        assertTrue(scheduleRange.getWeekDays()<0);
    }

    @Test
    void whenAddRestriction_givenValidRestriction_thenAdded() {
        RestrictionTuple restriction = new RestrictionTuple(LocalTime.of(10, 0), LocalTime.of(11, 0), 1);
        scheduleRange.addRestriction(restriction);
        assertEquals(1, scheduleRange.getRestrict().size());
    }

    @Test
    void whenAddRestriction_givenOutsideRange_thenException() {
        RestrictionTuple restriction = new RestrictionTuple(LocalTime.of(8, 0), LocalTime.of(9, 0), 1);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> scheduleRange.addRestriction(restriction));
        assertEquals("Restriction times are outside range times", thrown.getMessage());
    }

    @Test
    void whenRemoveRestrictionByStart_givenExistingStart_thenRemoved() {
        RestrictionTuple restriction = new RestrictionTuple(LocalTime.of(10, 0), LocalTime.of(11, 0), 1);
        scheduleRange.addRestriction(restriction);
        scheduleRange.removeRestrictionByStart(LocalTime.of(10, 0));
        assertTrue(scheduleRange.getRestrict().isEmpty());
    }

    @Test
    void whenRemoveRestrictionByStart_givenNonExistingStart_thenException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> scheduleRange.removeRestrictionByStart(LocalTime.of(12, 0)));
        assertEquals("Cannot remove a restriction that doesn't exist", thrown.getMessage());
    }

    @Test
    void whenCheckMinutes_givenValidRange_thenNoException() {
        assertDoesNotThrow(() -> scheduleRange.checkMinutes(LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    void whenCheckMinutes_givenBelowMinimum_thenException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> scheduleRange.checkMinutes(LocalTime.of(10, 0), LocalTime.of(10, 15)));
        assertEquals("Must make appointment to at least 30 minutes.", thrown.getMessage());
    }

    @Test
    void whenCheckMinutes_givenAboveMaximum_thenException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> scheduleRange.checkMinutes(LocalTime.of(10, 0), LocalTime.of(12, 30)));
        assertEquals("Must make appointment to at most 120 minutes.", thrown.getMessage());
    }

    @Test
    void whenCheckDays_givenMatchingDays_thenNoException() {
        assertDoesNotThrow(() -> scheduleRange.checkDays(null, new boolean[]{true, false, false, false, false, false, false}));
    }

    @Test
    void whenCheckDays_givenNonMatchingDays_thenException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> scheduleRange.checkDays(null, new boolean[]{false, false, false, false, false, false, true}));
        assertEquals("Appointment days do not match range days", thrown.getMessage());
    }
}

