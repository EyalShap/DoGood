package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import com.dogood.dogoodbackend.jparepos.AppointmentJPA;
import com.dogood.dogoodbackend.jparepos.HourRequestJPA;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
public class DatabaseSchedulingManager implements SchedulingManager{
    private HourRequestJPA hourRequestJPA;
    private AppointmentJPA appointmentJPA;

    public DatabaseSchedulingManager(HourRequestJPA hourRequestJPA, AppointmentJPA appointmentJPA) {
        this.hourRequestJPA = hourRequestJPA;
        this.appointmentJPA = appointmentJPA;
    }

    @Override
    public List<ScheduleAppointment> getAllAppointments() {
        return appointmentJPA.findAll();
    }

    @Override
    public List<ScheduleAppointment> getUserAppointments(String username, List<Integer> volunteeringIds) {
        List<ScheduleAppointment> appointments = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            appointments.addAll(appointmentJPA.findByUserIdAndVolunteeringId(username, volunteeringId));
        }
        return appointments;
    }

    @Override
    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date start, Date end, int minutesAllowed) {
        for(ScheduleAppointment appointment : appointmentJPA.findByUserIdAndVolunteeringId(username, volunteeringId)) {
            if(appointment.matchRange(start, end, minutesAllowed)){
                return appointment;
            }
        }
        return null;
    }

    @Override
    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date include, int minutesAllowed) {
        for(ScheduleAppointment appointment : appointmentJPA.findByUserIdAndVolunteeringId(username, volunteeringId)) {
            if(appointment.includesDate(include, minutesAllowed)){
                return appointment;
            }
        }
        return null;
    }

    @Override
    public List<HourApprovalRequest> getUserHourApproveRequests(String username, List<Integer> volunteeringIds) {
        List<HourApprovalRequest> requests = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            requests.addAll(hourRequestJPA.findByUserIdAndVolunteeringId(username,volunteeringId));
        }
        return requests.stream().filter(request -> !request.isApproved()).toList();
    }

    @Override
    public List<HourApprovalRequest> getApprovedUserHours(String username, List<Integer> volunteeringIds) {
        List<HourApprovalRequest> approvedHours = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            approvedHours.addAll(hourRequestJPA.findByUserIdAndVolunteeringId(username,volunteeringId));
        }
        return approvedHours.stream().filter(HourApprovalRequest::isApproved).toList();
    }

    @Override
    public List<HourApprovalRequest> getApprovedUserHours(String username) {
        return hourRequestJPA.findByUserIdAndApproved(username, true);
    }

    @Override
    public List<ScheduleAppointment> getVolunteeringAppointments(int volunteeringId) {
        return appointmentJPA.findByVolunteeringId(volunteeringId);
    }

    @Override
    public List<HourApprovalRequest> getVolunteeringHourApproveRequests(int volunteeringId) {
        return hourRequestJPA.findByVolunteeringId(volunteeringId).stream().filter(request -> !request.isApproved()).toList();
    }

    @Override
    public void makeAppointment(ScheduleAppointment appointment) {
        for(ScheduleAppointment other : appointmentJPA.findByUserIdAndVolunteeringId(appointment.getUserId(), appointment.getVolunteeringId())){
            if(appointment.intersect(other)){
                throw new IllegalArgumentException("You have an intersecting appointment within this range");
            }
        }
        appointmentJPA.save(appointment);
    }

    @Override
    public void addHourApprovalRequest(String username, int volunteeringId, Date start, Date end) {
        if(end.before(start)){
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        for(HourApprovalRequest request : hourRequestJPA.findByUserIdAndVolunteeringId(username, volunteeringId)){
            if(request.intersect(start, end)){
                throw new UnsupportedOperationException("A request by username " + username + " in this range already exists");
            }
        }
        hourRequestJPA.save(new HourApprovalRequest(username, volunteeringId, start, end));
    }

    @Override
    public void approveUserHours(String username, int volunteeringId, Date start, Date end) {
        HourApprovalRequest requestToApprove = null;
        for(HourApprovalRequest request : hourRequestJPA.findByUserIdAndVolunteeringId(username, volunteeringId)){
            if(request.getStartTime().getTime() == start.getTime() && request.getEndTime().getTime() == end.getTime()){
                requestToApprove = request;
            }
        }
        if(requestToApprove == null){
            throw new UnsupportedOperationException("There is no hour approval request for user " + username + " in volunteering " + volunteeringId + " from " + start + " to " + end);
        }
        requestToApprove.approve();
        hourRequestJPA.save(requestToApprove);
    }

    @Override
    public void denyUserHours(String username, int volunteeringId, Date start, Date end) {
        HourApprovalRequest requestToDeny = null;
        for(HourApprovalRequest request : hourRequestJPA.findByUserIdAndVolunteeringId(username, volunteeringId)){
            if(request.getStartTime().getTime() == start.getTime() && request.getEndTime().getTime() == end.getTime()){
                requestToDeny = request;
            }
        }
        if(requestToDeny == null){
            throw new UnsupportedOperationException("There is no hour approval request for user " + username + " in volunteering " + volunteeringId + " from " + start + " to " + end);
        }
        hourRequestJPA.delete(requestToDeny);
    }

    @Override
    public int getAmountOfAppointmentsInRestrict(int volunteeringId, int rangeId, RestrictionTuple r, boolean[] weekDays, LocalDate oneTime) {
        List<ScheduleAppointment> appointments = getVolunteeringAppointments(volunteeringId).stream().filter(appointment -> appointment.getRangeId() == rangeId).collect(Collectors.toList());
        int count = 0;
        for(ScheduleAppointment appointment : appointments){
            if(appointment.daysMatch(oneTime, weekDays) && r.intersect(appointment.getStartTime(), appointment.getEndTime())){
                count++;
            }
        }
        return count;
    }

    @Override
    public void removeAppointmentsAndRequestsForVolunteering(int volunteeringId) {
        hourRequestJPA.deleteByVolunteeringIdAndApproved(volunteeringId,false);
        appointmentJPA.deleteByVolunteeringId(volunteeringId);
    }

    @Override
    public void removeAppointmentsOfRange(int volunteeringId, int rID) {
        appointmentJPA.deleteByVolunteeringIdAndRangeId(volunteeringId, rID);
    }

    @Override
    public void cancelAppointment(String userId, int volunteeringId, LocalTime start) {
        try {
            appointmentJPA.deleteById(new UserVolunteerTimeKT(userId, volunteeringId, start));
        }catch (Exception e){
            throw new IllegalArgumentException("There is no appointment for volunteering " + volunteeringId + " by user " + userId + " that starts at " + start);
        }
    }

    @Override
    public void userLeave(int volunteeringId, String userId) {
        appointmentJPA.deleteByUserIdAndVolunteeringId(userId, volunteeringId);
        hourRequestJPA.deleteByUserIdAndVolunteeringIdAndApproved(userId, volunteeringId, false);
    }

    @Override
    public void updateRequestDescription(String userId, int volunteeringId, Date start, String newDescription) {
        HourApprovalRequest request = hourRequestJPA.getReferenceById(new UserVolunteerDateKT(userId,volunteeringId,start));
        request.setDescription(newDescription);
        hourRequestJPA.save(request);
    }
}
