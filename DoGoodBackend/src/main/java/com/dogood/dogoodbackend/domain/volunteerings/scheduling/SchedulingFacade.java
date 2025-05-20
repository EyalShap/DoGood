package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import com.dogood.dogoodbackend.domain.volunteerings.ScheduleRange;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Transactional
public class SchedulingFacade {
    private SchedulingManager manager;
    public SchedulingFacade(SchedulingManager manager) {
        this.manager = manager;
    }

    public boolean getHasAppointmentAtRoughStart(String username, int volunteeringId, Date start, int minutesAllowed){
        List<ScheduleAppointment> scheduleAppointments = manager.getUserAppointments(username, List.of(volunteeringId));
        for (ScheduleAppointment scheduleAppointment : scheduleAppointments) {
            if(scheduleAppointment.matchStart(start, minutesAllowed)){
                return true;
            }
        }
        return false;
    }

    public DatePair convertRoughRangeToAppointmentRange(String username, int volunteeringId, Date start, Date end, int minutesAllowed){
        ScheduleAppointment appointment = getUserAppointmentInRange(username, volunteeringId, start, end, minutesAllowed);
        if(appointment == null){
            throw new IllegalArgumentException("Appointment not found for the specified scan times in volunteering " + volunteeringId + " for user " + username + ", please request manually");
        }
        return appointment.getDefiniteRange(start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public DatePair convertSingleTimeToAppointmentRange(String username, int volunteeringId, Date single, int minutesAllowed){
        ScheduleAppointment appointment = getUserAppointmentInRange(username, volunteeringId, single, minutesAllowed);
        if(appointment == null){
            throw new IllegalArgumentException("Appointment not found for the specified scan times in volunteering " + volunteeringId + " for user " + username + ", please request manually");
        }
        return appointment.getDefiniteRange(single.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
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

    public void denyUserHours(String username, int volunteeringId, Date start, Date end){
        manager.denyUserHours(username, volunteeringId, start, end);
    }

    private boolean checkIfFull(int volunteeringId, int rangeId, RestrictionTuple r, boolean[] weekDays, LocalDate oneTime){
        return manager.getAmountOfAppointmentsInRestrict(volunteeringId, rangeId, r, weekDays, oneTime) >= r.getAmount();
    }

    public void makeAppointment(String userId, int volunteeringId, ScheduleRange range, LocalTime start, LocalTime end, boolean[] weekDays, LocalDate oneTime){
        for(RestrictionTuple r : range.checkCollision(start, end)){
            if(checkIfFull(volunteeringId, range.getId(), r.intersection(start, end), weekDays, oneTime)){
                throw new UnsupportedOperationException("The range " + range.getId() + " between " + start + " and " + end + " is full on the specified dates");
            }
        }
        if(!start.isBefore(end)){
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if(start.isBefore(range.getStartTime()) || end.isAfter(range.getEndTime())){
            throw new IllegalArgumentException("Appointment doesn't match selected range");
        }
        range.checkMinutes(start,end);
        range.checkDays(oneTime, weekDays);
        ScheduleAppointment scheduleAppointment = new ScheduleAppointment(userId, volunteeringId, range.getId(), start, end, oneTime, weekDays);
        manager.makeAppointment(scheduleAppointment);
    }

    public List<HourApprovalRequest> getUserApprovedHours(String userId, List<Integer> volunteeringIds){
        return manager.getApprovedUserHours(userId, volunteeringIds);
    }

    public List<HourApprovalRequest> getHourApprovalRequests(int volunteeringId){
        return manager.getVolunteeringHourApproveRequests(volunteeringId);
    }

    public List<ScheduleAppointmentDTO> getUserAppointments(String userId, int volunteeringId){
        return manager.getUserAppointments(userId, List.of(volunteeringId)).stream().map(sched -> sched.getDTO()).toList();
    }

    public List<ScheduleAppointmentDTO> getUserAppointments(String userId, List<Integer> volunteeringIds){
        return manager.getUserAppointments(userId, volunteeringIds).stream().map(sched -> sched.getDTO()).toList();
    }

    public void removeAppointmentsAndRequestsForVolunteering(int volunteeringId) {
        manager.removeAppointmentsAndRequestsForVolunteering(volunteeringId);
    }

    public void removeAppointmentsOfRange(int volunteeringId, int rID) {
        manager.removeAppointmentsOfRange(volunteeringId, rID);
    }

    public void cancelAppointment(String userId, int volunteeringId, LocalTime start) {
        manager.cancelAppointment(userId, volunteeringId, start);
    }

    public void updateRequestDescription(String userId, int volunteeringId, Date start, String newDescription){
        manager.updateRequestDescription(userId, volunteeringId, start, newDescription);
    }

    public List<ScheduleAppointment> getUpcomingAppointments(){
        return manager.getAllAppointments().stream().filter(ScheduleAppointment::isUpcoming).toList();
    }

    public void userLeave(int volunteeringId, String userId) {
        manager.userLeave(volunteeringId, userId);
    }
}
