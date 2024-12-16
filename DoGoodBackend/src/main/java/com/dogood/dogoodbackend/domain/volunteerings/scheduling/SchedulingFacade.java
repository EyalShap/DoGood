package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.ZoneId;
import java.util.Date;

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

    public DatePair convertSingleTimeToAppointmentRange(String username, int volunteeringId, Date single){
        ScheduleAppointment appointment = getUserAppointmentInRange(username, volunteeringId, single);
        if(appointment == null){
            throw new IllegalArgumentException("Appointment not found for the specified scan times in volunteering " + volunteeringId + " for user " + username + ", please request manually");
        }
        DatePair p = appointment.getDefiniteRange(single.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        return p;
    }

    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date start, Date end, int minutesAllowed){
        return manager.getUserAppointmentInRange(username, volunteeringId, start, end, minutesAllowed);
    }

    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date include){
        return manager.getUserAppointmentInRange(username, volunteeringId, include);
    }

    public void addHourApprovalRequest(String username, int volunteeringId, Date start, Date end){
        manager.addHourApprovalRequest(username, volunteeringId, start, end);
    }

    public void approveUserHours(String username, int volunteeringId, Date start, Date end){
        manager.approveUserHours(username, volunteeringId, start, end);
    }
}
