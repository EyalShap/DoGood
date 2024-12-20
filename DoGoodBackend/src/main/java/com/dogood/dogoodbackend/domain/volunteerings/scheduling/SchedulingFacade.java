package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class SchedulingFacade {
    private SchedulingManager manager;
    public SchedulingFacade(SchedulingManager manager) {
        this.manager = manager;
    }

    public DatePair convertRoughRangeToAppointmentRange(String username, int volunteeringId, Date start, Date end, int minutesAllowed){
        ScheduleAppointment appointment = getUserAppointmentInRange(username, volunteeringId, start, end, minutesAllowed);
        if(appointment == null){
            throw new IllegalArgumentException("Appointment not found for the specified scan times in volunteering " + volunteeringId + " for user " + username + ", please request manually");
        }
        DatePair p = appointment.getDefiniteRange(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        return p;
    }

    public DatePair convertSingleTimeToAppointmentRange(String username, int volunteeringId, Date single, int minutesAllowed){
        ScheduleAppointment appointment = getUserAppointmentInRange(username, volunteeringId, single, minutesAllowed);
        if(appointment == null){
            throw new IllegalArgumentException("Appointment not found for the specified scan times in volunteering " + volunteeringId + " for user " + username + ", please request manually");
        }
        DatePair p = appointment.getDefiniteRange(single.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        return p;
    }

    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date start, Date end, int minutesAllowed){
        return manager.getUserAppointmentInRange(username, volunteeringId, start, end, minutesAllowed);
    }

    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date include, int minutesAllowed){
        return manager.getUserAppointmentInRange(username, volunteeringId, include, minutesAllowed);
    }

    public void addHourApprovalRequest(String username, int volunteeringId, Date start, Date end){
        manager.addHourApprovalRequest(username, volunteeringId, start, end);
    }

    public void approveUserHours(String username, int volunteeringId, Date start, Date end){
        manager.approveUserHours(username, volunteeringId, start, end);
    }

    private boolean checkIfFull(int volunteeringId, int rangeId, RestrictionTuple r, boolean[] weekDays, LocalDate oneTime){
        return manager.getAmountOfAppointmentsInRestrict(volunteeringId, rangeId, r, weekDays, oneTime) >= r.getAmount();
    }

    public void makeAppointment(String userId, int volunteeringId, int rangeId, LocalTime start, LocalTime end, boolean[] weekDays, LocalDate oneTime, List<RestrictionTuple> restrictions){
        for(RestrictionTuple r : restrictions){
            if(checkIfFull(volunteeringId, rangeId, r.intersection(start, end), weekDays, oneTime)){
                throw new UnsupportedOperationException("The range " + rangeId + " between " + start + " and " + end + " is full on the specified dates");
            }
        }
        ScheduleAppointment scheduleAppointment = new ScheduleAppointment(userId, volunteeringId, rangeId, start, end);
        scheduleAppointment.setWeekDays(weekDays);
        scheduleAppointment.setOneTime(oneTime);
        manager.makeAppointment(scheduleAppointment);
    }
}
