package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class ScheduleAppointment {
    private String userId;
    private int rangeId;
    private LocalTime startTime;
    private LocalTime endTime;
    private int[] weekDays;
    private LocalDate oneTime;

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

    public LocalDate getOneTime() {
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

    public void setOneTime(LocalDate oneTime) {
        this.oneTime = oneTime;
    }

    public DatePair getDefiniteRange(LocalDate day){
        if(oneTime != null && day != null &&
                (day.getDayOfMonth() != oneTime.getDayOfMonth() ||
                        day.getMonth() != oneTime.getMonth() ||
                        day.getYear() != oneTime.getYear())){
            throw new UnsupportedOperationException("Given day doesn't match one time day");
        }
        LocalDateTime startDateTime = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), startTime.getHour(), startTime.getMinute());
        LocalDateTime endDateTime = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), endTime.getHour(), endTime.getMinute());

        return new DatePair(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }
}
