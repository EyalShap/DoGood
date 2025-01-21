package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import com.dogood.dogoodbackend.domain.volunteerings.ScheduleRangeDTO;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

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

    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;

    private transient boolean[] weekDays;

    private LocalDate oneTime;

    public ScheduleAppointment(String userId, int volunteeringId, int rangeId, LocalTime startTime, LocalTime endTime) {
        this.userId = userId;
        this.rangeId = rangeId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
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
        if (weekDays != null && weekDays.length != 7) {
            throw new IllegalArgumentException("weekDays.length != 7");
        }
        this.weekDays = weekDays;
        if(weekDays != null){
            oneTime = null;
            sunday = weekDays[0];
            monday = weekDays[1];
            tuesday = weekDays[2];
            wednesday = weekDays[3];
            thursday = weekDays[4];
            friday = weekDays[5];
            saturday = weekDays[6];
        }
        if(weekDays == null){
            sunday = false;
            monday = false;
            tuesday = false;
            wednesday = false;
            thursday = false;
            friday = false;
            saturday = false;
        }
    }

    public void setOneTime(LocalDate oneTime) {
        this.oneTime = oneTime;
        if(oneTime != null){
            weekDays = null;
            sunday = false;
            monday = false;
            tuesday = false;
            wednesday = false;
            thursday = false;
            friday = false;
            saturday = false;
        }
    }

    public boolean includesDate(Date include, int minutesAllowed){
        LocalDateTime includeDateTime = include.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if(oneTime != null && !oneTime.isEqual(includeDateTime.toLocalDate())){
            return false;
        }
        if(weekDays != null && !weekDays[includeDateTime.getDayOfWeek().getValue()%7]){
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
        if(weekDays != null && !weekDays[startDateTime.getDayOfWeek().getValue()%7]){
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
        if(weekDays != null && !weekDays[day.getDayOfWeek().getValue()%7]){
            throw new UnsupportedOperationException("Given day doesn't match week day");
        }
        LocalDateTime startDateTime = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), startTime.getHour(), startTime.getMinute());
        LocalDateTime endDateTime = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), endTime.getHour(), endTime.getMinute());

        return new DatePair(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }

    public ScheduleAppointmentDTO getDTO(){
        boolean[] weekDaysCopy;
        if(weekDays != null){
            weekDaysCopy  = Arrays.copyOf(weekDays,weekDays.length);
        }else{
            weekDaysCopy = null;
        }
        return new ScheduleAppointmentDTO(userId, volunteeringId, rangeId, startTime, endTime, weekDaysCopy, oneTime);
    }

    public boolean intersect(ScheduleAppointment other) {
        if(oneTime != null && other.oneTime != null) {
            return oneTime.isEqual(other.getOneTime()) && !(other.getStartTime().isAfter(this.endTime) || other.getEndTime().isBefore(this.startTime));
        }
        if(weekDays != null && other.weekDays != null) {
            for(int i = 0; i < 7; i++){
                if(weekDays[i] && other.weekDays[i]){
                    return !(other.getStartTime().isAfter(this.endTime) || other.getEndTime().isBefore(this.startTime));
                }
            }
        }
        if(weekDays != null){
            return weekDays[other.getOneTime().getDayOfWeek().getValue()%7] && !(other.getStartTime().isAfter(this.endTime) || other.getEndTime().isBefore(this.startTime));
        }
        return other.getWeekDays()[oneTime.getDayOfWeek().getValue()%7] && !(other.getStartTime().isAfter(this.endTime) || other.getEndTime().isBefore(this.startTime));
    }

    @PostLoad
    private void loadWeekDays(){
        if(sunday || monday || tuesday || wednesday || thursday || friday || saturday){
            weekDays = new boolean[]{sunday, monday, tuesday, wednesday, thursday, friday, saturday};
        }
    }
}
