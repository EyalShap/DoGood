package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleRange {
    private final int id;
    private LocalTime startTime;
    private LocalTime endTime;
    private int minimumAppointmentMinutes;
    private int maximumAppointmentMinutes;
    private List<RestrictionTuple> restrict;
    private boolean[] weekDays;
    private LocalDate oneTime;

    public ScheduleRange(int id, LocalTime startTime, LocalTime endTime, int minimumAppointmentMinutes, int maximumAppointmentMinutes) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minimumAppointmentMinutes = minimumAppointmentMinutes;
        this.maximumAppointmentMinutes = maximumAppointmentMinutes;
        this.restrict = new LinkedList<>();
    }

    public boolean[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(boolean[] weekDays) {
        this.weekDays = weekDays;
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
            weekDays = null;
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
        boolean[] weekDaysCopy;
        if(weekDays != null){
            weekDaysCopy  = Arrays.copyOf(weekDays,weekDays.length);
        }else{
            weekDaysCopy = null;
        }
        return new ScheduleRangeDTO(id, startTime, endTime, minimumAppointmentMinutes, maximumAppointmentMinutes, restrictCopy,weekDaysCopy,oneTime);
    }
}
