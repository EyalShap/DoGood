package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.WeekArray;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@IdClass(IdVolunteeringPK.class)
public class ScheduleRange {
    @Id
    @Column(name="id")
    private int id;
    @Id
    @Column(name = "volunteering_id", nullable = false)
    private int volunteeringId;
    private LocalTime startTime;
    private LocalTime endTime;
    private int minimumAppointmentMinutes;
    private int maximumAppointmentMinutes;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<RestrictionTuple> restrict;

    private byte weekDays;
    private LocalDate oneTime;

    public ScheduleRange(int id, int volunteeringId, LocalTime startTime, LocalTime endTime, int minimumAppointmentMinutes, int maximumAppointmentMinutes, boolean[] weekDays, LocalDate oneTime) {
        if(oneTime != null && weekDays != null) {
            throw new IllegalArgumentException("Cant have both week days and one time date");
        }
        if(oneTime == null && weekDays == null) {
            throw new IllegalArgumentException("Must have week days or one time");
        }
        if (weekDays != null && weekDays.length != 7) {
            throw new IllegalArgumentException("weekDays.length != 7");
        }
        this.id = id;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minimumAppointmentMinutes = minimumAppointmentMinutes;
        this.maximumAppointmentMinutes = maximumAppointmentMinutes;
        this.restrict = new LinkedList<>();
        this.oneTime = oneTime;
        this.weekDays = convertDayArrayToByte(weekDays);
    }

    public ScheduleRange() {

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

    public byte getWeekDays() {
        return weekDays;
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

    public LocalDate getOneTime() {
        return oneTime;
    }

    public void setOneTime(LocalDate oneTime) {
        this.oneTime = oneTime;
        if(oneTime != null){
            weekDays = -1;
        }
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


    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setMinimumAppointmentMinutes(int minimumAppointmentMinutes) {
        this.minimumAppointmentMinutes = minimumAppointmentMinutes;
    }

    public void setMaximumAppointmentMinutes(int maximumAppointmentMinutes) {
        this.maximumAppointmentMinutes = maximumAppointmentMinutes;
    }

    public void addRestriction(RestrictionTuple restriction) {
        if(restriction.getStartTime().isBefore(startTime) || restriction.getEndTime().isAfter(endTime)){
            throw new IllegalArgumentException("Restriction times are outside range times");
        }
        for(RestrictionTuple restrictionTuple : restrict) {
            if(restrictionTuple.intersect(restriction.getStartTime(), restriction.getEndTime())){
                throw new IllegalArgumentException("Cannot add restriction that intersects an existing one");
            }
        }
        restrict.add(restriction);
    }

    public void removeRestrictionByStart(LocalTime startTime) {
        RestrictionTuple restriction = null;
        for(RestrictionTuple restrictionTuple : restrict) {
            if(restrictionTuple.getStartTime().equals(startTime)){
                restriction=restrictionTuple;
            }
        }
        if(restriction == null){
            throw new IllegalArgumentException("Cannot remove a restriction that doesn't exist");
        }
        restrict.remove(restriction);
    }

    public List<RestrictionTuple> checkCollision(LocalTime startTime, LocalTime endTime){
        List<RestrictionTuple> collides = new LinkedList<>();
        for(RestrictionTuple restrictionTuple : restrict) {
            if(restrictionTuple.intersect(startTime,endTime)){
                collides.add(restrictionTuple);
            }
        }
        return collides;
    }

    public ScheduleRangeDTO getDTO(){
        List<RestrictionTuple> restrictCopy = restrict.stream().map(restrictionTuple -> new RestrictionTuple(restrictionTuple.getStartTime(), restrictionTuple.getEndTime(), restrictionTuple.getAmount())).collect(Collectors.toList());
        return new ScheduleRangeDTO(id, startTime, endTime, minimumAppointmentMinutes, maximumAppointmentMinutes, restrictCopy,getDayArray(),oneTime);
    }

    public void checkMinutes(LocalTime startTime, LocalTime endTime) {
        if(minimumAppointmentMinutes == -1 && maximumAppointmentMinutes == -1){
            return;
        }
        long minutes = startTime.until(endTime, ChronoUnit.MINUTES);
        if(minimumAppointmentMinutes > -1 && minutes < minimumAppointmentMinutes){
            throw new IllegalArgumentException("Must make appointment to at least " + minimumAppointmentMinutes + " minutes.");
        }

        if(maximumAppointmentMinutes > -1 && minutes > maximumAppointmentMinutes){
            throw new IllegalArgumentException("Must make appointment to at most " + maximumAppointmentMinutes + " minutes.");
        }
        if(startTime.isBefore(this.startTime) || endTime.isAfter(this.endTime)){
            throw new IllegalArgumentException("Appointment must be within range");
        }
    }

    private boolean daysMatch(LocalDate oneTime, boolean[] weekDays) {
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

    public void checkDays(LocalDate oneTime, boolean[] weekDays) {
        if(!daysMatch(oneTime, weekDays)){
            throw new IllegalArgumentException("Appointment days do not match range days");
        }
    }
}
