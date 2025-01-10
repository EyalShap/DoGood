package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import com.dogood.dogoodbackend.domain.volunteerings.ScheduleRangeDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

public class ScheduleAppointmentDTO {
    private String userId;
    private int rangeId;
    private int volunteeringId;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean[] weekDays;
    private LocalDate oneTime;

    public ScheduleAppointmentDTO(String userId, int volunteeringId, int rangeId, LocalTime startTime, LocalTime endTime, boolean[] weekDays, LocalDate oneTime) {
        this.userId = userId;
        this.rangeId = rangeId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.weekDays = weekDays;
        this.oneTime = oneTime;
    }

    public String getUserId() {
        return userId;
    }

    public int getRangeId() {
        return rangeId;
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean[] getWeekDays() {
        return weekDays;
    }

    public LocalDate getOneTime() {
        return oneTime;
    }
}
