package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ScheduleRange {
    private final int id;
    private LocalTime startTime;
    private LocalTime endTime;
    private int minimumAppointmentMinutes;
    private int maximumAppointmentMinutes;
    private List<RestrictionTuple> restrict;
    private int[] weekDays;
    private Date oneTime;

    public ScheduleRange(int id, LocalTime startTime, LocalTime endTime, int minimumAppointmentMinutes, int maximumAppointmentMinutes) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minimumAppointmentMinutes = minimumAppointmentMinutes;
        this.maximumAppointmentMinutes = maximumAppointmentMinutes;
        this.restrict = new LinkedList<>();
    }

    public int[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(int[] weekDays) {
        this.weekDays = weekDays;
    }

    public Date getOneTime() {
        return oneTime;
    }

    public void setOneTime(Date oneTime) {
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
}
