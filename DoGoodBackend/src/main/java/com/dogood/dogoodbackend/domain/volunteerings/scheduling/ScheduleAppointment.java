package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static java.time.temporal.ChronoUnit.MINUTES;

public class ScheduleAppointment {
    private String userId;
    private int rangeId;
    private int volunteeringId;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean[] weekDays;
    private LocalDate oneTime;

    public ScheduleAppointment(String userId, int volunteeringId, int rangeId, LocalTime startTime, LocalTime endTime) {
        this.userId = userId;
        this.rangeId = rangeId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getVolunteeringId() {
        return volunteeringId;
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

    public boolean[] getWeekDays() {
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

    public void setWeekDays(boolean[] weekDays) {
        this.weekDays = weekDays;
        if(weekDays != null){
            oneTime = null;
        }
    }

    public void setOneTime(LocalDate oneTime) {
        this.oneTime = oneTime;
        if(oneTime != null){
            weekDays = null;
        }
    }

    public boolean includesDate(Date include, int minutesAllowed){
        LocalDateTime includeDateTime = include.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if(oneTime != null && !oneTime.isEqual(includeDateTime.toLocalDate())){
            return false;
        }
        if(weekDays != null && !weekDays[includeDateTime.getDayOfWeek().getValue()]){
            return false;
        }
        return MINUTES.between(includeDateTime.toLocalTime(), startTime) <= minutesAllowed || MINUTES.between(includeDateTime.toLocalTime(), endTime) <= minutesAllowed
                || (startTime.isBefore(includeDateTime.toLocalTime()) && endTime.isAfter(includeDateTime.toLocalTime()));
    }

    public boolean matchRange(Date start, Date end, int minutesAllowed){
        LocalDateTime startDateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDateTime = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if(!startDateTime.toLocalDate().isEqual(endDateTime.toLocalDate())){
            return false;
        }
        if(oneTime != null && !oneTime.isEqual(startDateTime.toLocalDate())){
            return false;
        }
        if(weekDays != null && !weekDays[startDateTime.getDayOfWeek().getValue()]){
            return false;
        }
        return MINUTES.between(startDateTime.toLocalTime(), startTime) <= minutesAllowed && MINUTES.between(endDateTime.toLocalTime(), endTime) <= minutesAllowed;
    }

    public DatePair getDefiniteRange(LocalDate day){
        if(oneTime != null && day != null &&
                (day.getDayOfMonth() != oneTime.getDayOfMonth() ||
                        day.getMonth() != oneTime.getMonth() ||
                        day.getYear() != oneTime.getYear())){
            throw new UnsupportedOperationException("Given day doesn't match one time day");
        }
        if(weekDays != null && !weekDays[day.getDayOfWeek().getValue()]){
            throw new UnsupportedOperationException("Given day doesn't match week day");
        }
        LocalDateTime startDateTime = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), startTime.getHour(), startTime.getMinute());
        LocalDateTime endDateTime = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), endTime.getHour(), endTime.getMinute());

        return new DatePair(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }
}
