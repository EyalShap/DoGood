package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static java.time.temporal.ChronoUnit.MINUTES;

@Entity
@IdClass(UserVolunteerTimeKT.class)
public class ScheduleAppointment {
    @Id
    private String userId;
    private int rangeId;

    @Id
    private int volunteeringId;

    @Id
    private LocalTime startTime;
    private LocalTime endTime;

    private byte weekDays;

    private LocalDate oneTime;

    public ScheduleAppointment(String userId, int volunteeringId, int rangeId, LocalTime startTime, LocalTime endTime, LocalDate oneTime, boolean[] weekDays) {
        if(oneTime != null && weekDays != null) {
            throw new IllegalArgumentException("Cant have both week days and one time date");
        }
        if(oneTime == null && weekDays == null) {
            throw new IllegalArgumentException("Must have week days or one time");
        }
        if (weekDays != null && weekDays.length != 7) {
            throw new IllegalArgumentException("weekDays.length != 7");
        }
        this.userId = userId;
        this.rangeId = rangeId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.oneTime = oneTime;
        this.weekDays = convertDayArrayToByte(weekDays);
    }

    private byte convertDayArrayToByte(boolean[] weekDays){
        if(weekDays == null){
            return -1;
        }
        byte days = 0;
        for (int i = 6; i >= 0; i--) {
            days *= 2;
            days += weekDays[i] ? 1 : 0;
        }
        return days;
    }

    private boolean[] getDayArray(){
        if(weekDays == -1){
            return null;
        }
        boolean[] dayArray = new boolean[7];
        for (int i = 0; i < 7; i++) {
            dayArray[i] = valueAtDay(i);
        }
        return dayArray;
    }

    private boolean valueAtDay(int day){
        if(weekDays < 0){
            return false;
        }
        return (weekDays >> day)%2 == 1;
    }

    public ScheduleAppointment() {}

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

    public byte getWeekDays() {
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
        if (weekDays != null && weekDays.length != 7) {
            throw new IllegalArgumentException("weekDays.length != 7");
        }
        this.weekDays = convertDayArrayToByte(weekDays);
        if(weekDays != null) {
            oneTime = null;
        }
    }

    public void setOneTime(LocalDate oneTime) {
        this.oneTime = oneTime;
        if(oneTime != null){
            weekDays = -1;
        }
    }

    public boolean includesDate(Date include, int minutesAllowed){
        LocalDateTime includeDateTime = include.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if(oneTime != null && !oneTime.isEqual(includeDateTime.toLocalDate())){
            return false;
        }
        if(weekDays >= 0 && !valueAtDay(includeDateTime.getDayOfWeek().getValue()%7)){
            return false;
        }
        return Math.abs(MINUTES.between(includeDateTime.toLocalTime(), startTime)) <= minutesAllowed || Math.abs(MINUTES.between(includeDateTime.toLocalTime(), endTime)) <= minutesAllowed
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
        if(weekDays >= 0 && !valueAtDay(startDateTime.getDayOfWeek().getValue()%7)){
            return false;
        }
        return Math.abs(MINUTES.between(startDateTime.toLocalTime(), startTime)) <= minutesAllowed && Math.abs(MINUTES.between(endDateTime.toLocalTime(), endTime)) <= minutesAllowed;
    }

    public DatePair getDefiniteRange(LocalDate day){
        if(oneTime != null && day != null &&
                (day.getDayOfMonth() != oneTime.getDayOfMonth() ||
                        day.getMonth() != oneTime.getMonth() ||
                        day.getYear() != oneTime.getYear())){
            throw new UnsupportedOperationException("Given day doesn't match one time day");
        }
        if(weekDays >= 0 && !valueAtDay(day.getDayOfWeek().getValue()%7)){
            throw new UnsupportedOperationException("Given day doesn't match week day");
        }
        LocalDateTime startDateTime = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), startTime.getHour(), startTime.getMinute());
        LocalDateTime endDateTime = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), endTime.getHour(), endTime.getMinute());

        return new DatePair(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }


    public ScheduleAppointmentDTO getDTO(){
        return new ScheduleAppointmentDTO(userId, volunteeringId, rangeId, startTime, endTime, getDayArray(), oneTime);
    }

    public boolean intersect(ScheduleAppointment other) {
        if(oneTime != null && other.oneTime != null) {
            return oneTime.isEqual(other.getOneTime()) && !(other.getStartTime().isAfter(this.endTime) || other.getEndTime().isBefore(this.startTime));
        }
        if(weekDays >= 0 && other.weekDays >= 0) {
            for(int i = 0; i < 7; i++){
                if(valueAtDay(i) && other.valueAtDay(i)){
                    return !(other.getStartTime().isAfter(this.endTime) || other.getEndTime().isBefore(this.startTime));
                }
            }
            return false;
        }
        if(weekDays >= 0){
            return valueAtDay(other.getOneTime().getDayOfWeek().getValue()%7) && !(other.getStartTime().isAfter(this.endTime) || other.getEndTime().isBefore(this.startTime));
        }
        return other.valueAtDay(oneTime.getDayOfWeek().getValue()%7) && !(other.getStartTime().isAfter(this.endTime) || other.getEndTime().isBefore(this.startTime));
    }

    public boolean daysMatch(LocalDate oneTime, boolean[] weekDays) {
        if(oneTime != null){
            if(this.oneTime != null){
                return oneTime.isEqual(this.oneTime);
            }
            return valueAtDay(oneTime.getDayOfWeek().getValue()%7);
        }
        if(this.oneTime != null){
            return false;
        }
        for(int i = 0; i < 7; i++){
            if(weekDays[i] && !valueAtDay(i)){
                return false;
            }
        }
        return true;
    }

    public boolean matchStart(Date start, int minutesAllowed) {
        LocalDateTime startDateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if(oneTime != null && !oneTime.isEqual(startDateTime.toLocalDate())){
            return false;
        }
        if(weekDays >= 0 && !valueAtDay(startDateTime.getDayOfWeek().getValue()%7)){
            return false;
        }
        return Math.abs(MINUTES.between(startDateTime.toLocalTime(), startTime)) <= minutesAllowed;
    }

    public boolean isUpcoming(){
        LocalDateTime now = LocalDateTime.now();
        if(weekDays > 0 && !valueAtDay(now.getDayOfWeek().getValue()%7)){
            return false;
        }
        if(oneTime != null && !now.toLocalDate().isEqual(oneTime)){
            return false;
        }
        return now.toLocalTime().isBefore(startTime) && MINUTES.between(now.toLocalTime(), startTime) <= 60;
    }
}
