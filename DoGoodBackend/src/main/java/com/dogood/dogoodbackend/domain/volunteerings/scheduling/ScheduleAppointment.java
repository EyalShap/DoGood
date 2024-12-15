package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalTime;
import java.util.Date;

public class ScheduleAppointment {
    private String userId;
    private int rangeId;
    private LocalTime startTime;
    private LocalTime endTime;
    private int[] weekDays;
    private Date oneTime;

    public ScheduleAppointment(String userId, int rangeId, LocalTime startTime, LocalTime endTime) {
        this.userId = userId;
        this.rangeId = rangeId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getUserId() {
        return userId;
    }

    public int getRangeId() {
        return rangeId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int[] getWeekDays() {
        return weekDays;
    }

    public Date getOneTime() {
        return oneTime;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRangeId(int rangeId) {
        this.rangeId = rangeId;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setWeekDays(int[] weekDays) {
        this.weekDays = weekDays;
    }

    public void setOneTime(Date oneTime) {
        this.oneTime = oneTime;
    }
}
