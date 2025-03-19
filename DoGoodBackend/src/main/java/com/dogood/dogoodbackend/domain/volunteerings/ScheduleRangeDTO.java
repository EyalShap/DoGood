package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

public class ScheduleRangeDTO {
    private final int id;
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime startTime;
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime endTime;
    private int minimumAppointmentMinutes;
    private int maximumAppointmentMinutes;
    private List<RestrictionTuple> restrict;
    private boolean[] weekDays;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate oneTime;

    public ScheduleRangeDTO(int id, LocalTime startTime, LocalTime endTime, int minimumAppointmentMinutes, int maximumAppointmentMinutes, List<RestrictionTuple> restrict, boolean[] weekDays, LocalDate oneTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minimumAppointmentMinutes = minimumAppointmentMinutes;
        this.maximumAppointmentMinutes = maximumAppointmentMinutes;
        this.restrict = restrict;
        this.weekDays = weekDays;
        this.oneTime = oneTime;
    }

    public int getId() {
        return id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getMinimumAppointmentMinutes() {
        return minimumAppointmentMinutes;
    }

    public int getMaximumAppointmentMinutes() {
        return maximumAppointmentMinutes;
    }

    public List<RestrictionTuple> getRestrict() {
        return restrict;
    }

    public boolean[] getWeekDays() {
        return weekDays;
    }

    public LocalDate getOneTime() {
        return oneTime;
    }
}
