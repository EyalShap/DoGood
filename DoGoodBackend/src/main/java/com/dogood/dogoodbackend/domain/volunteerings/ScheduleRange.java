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
    private int volunteeringId;
    private LocalTime startTime;
    private LocalTime endTime;
    private int minimumAppointmentMinutes;
    private int maximumAppointmentMinutes;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<RestrictionTuple> restrict;

    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;

    private transient boolean[] weekDays;
    private LocalDate oneTime;

    public ScheduleRange(int id, int volunteeringId, LocalTime startTime, LocalTime endTime, int minimumAppointmentMinutes, int maximumAppointmentMinutes) {
        this.id = id;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minimumAppointmentMinutes = minimumAppointmentMinutes;
        this.maximumAppointmentMinutes = maximumAppointmentMinutes;
        this.restrict = new LinkedList<>();
    }

    public ScheduleRange() {

    }

    public boolean[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(boolean[] weekDays) {
        this.weekDays = weekDays;
        if(weekDays != null) {
            oneTime = null;
            sunday = weekDays[0];
            monday = weekDays[1];
            tuesday = weekDays[2];
            wednesday = weekDays[3];
            thursday = weekDays[4];
            friday = weekDays[5];
            saturday = weekDays[6];
        }else{
            sunday = false;
            monday = false;
            tuesday = false;
            wednesday = false;
            thursday = false;
            friday = false;
            saturday = false;
        }
    }

    public LocalDate getOneTime() {
        return oneTime;
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
    }

    @PostLoad
    private void loadWeekDays(){
        if(sunday || monday || tuesday || wednesday || thursday || friday || saturday){
            weekDays = new boolean[]{sunday, monday, tuesday, wednesday, thursday, friday, saturday};
        }
    }
}
