package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import com.dogood.dogoodbackend.jparepos.AppointmentJPA;
import com.dogood.dogoodbackend.jparepos.ApprovedHoursJPA;
import com.dogood.dogoodbackend.jparepos.HourRequestJPA;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
public class DatabaseSchedulingManager implements SchedulingManager{
    private HourRequestJPA hourRequestJPA;
    private AppointmentJPA appointmentJPA;
    private ApprovedHoursJPA approvedHoursJPA;

    public DatabaseSchedulingManager(HourRequestJPA hourRequestJPA, AppointmentJPA appointmentJPA, ApprovedHoursJPA approvedHoursJPA) {
        this.hourRequestJPA = hourRequestJPA;
        this.appointmentJPA = appointmentJPA;
        this.approvedHoursJPA = approvedHoursJPA;
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
    public List<HourApprovalRequests> getUserHourApproveRequests(String username, List<Integer> volunteeringIds) {
        List<HourApprovalRequests> requests = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            requests.addAll(hourRequestJPA.findByUserIdAndVolunteeringId(username,volunteeringId));
        }
        return requests;
    }

    @Override
    public List<ApprovedHours> getApprovedUserHours(String username, List<Integer> volunteeringIds) {
        List<ApprovedHours> approvedHours = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            approvedHours.addAll(approvedHoursJPA.findByUserIdAndVolunteeringId(username,volunteeringId));
        }
        return approvedHours;
    }

    @Override
    public List<ScheduleAppointment> getVolunteeringAppointments(int volunteeringId) {
        return appointmentJPA.findByVolunteeringId(volunteeringId);
    }

    @Override
    public List<HourApprovalRequests> getVolunteeringHourApproveRequests(int volunteeringId) {
        return hourRequestJPA.findByVolunteeringId(volunteeringId);
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
        for(HourApprovalRequests request : hourRequestJPA.findByUserIdAndVolunteeringId(username, volunteeringId)){
            if(request.intersect(start, end)){
                throw new UnsupportedOperationException("A request by username " + username + " in this range already exists");
            }
        }
        hourRequestJPA.save(new HourApprovalRequests(username, volunteeringId, start, end));
    }

    private void addApproval(String username, int volunteeringId, Date start, Date end) {
        approvedHoursJPA.save(new ApprovedHours(username, volunteeringId, start, end));
    }

    @Override
    public void approveUserHours(String username, int volunteeringId, Date start, Date end) {
        HourApprovalRequests requestToApprove = null;
        for(HourApprovalRequests request : hourRequestJPA.findByUserIdAndVolunteeringId(username, volunteeringId)){
            if(request.getStartTime().getTime() == start.getTime() && request.getEndTime().getTime() == end.getTime()){
                requestToApprove = request;
            }
        }
        if(requestToApprove == null){
            throw new UnsupportedOperationException("There is no hour approval request for user " + username + " in volunteering " + volunteeringId + " from " + start + " to " + end);
        }
        addApproval(username, volunteeringId, start, end);
        hourRequestJPA.delete(requestToApprove);
    }

    @Override
    public void denyUserHours(String username, int volunteeringId, Date start, Date end) {
        HourApprovalRequests requestToApprove = null;
        for(HourApprovalRequests request : hourRequestJPA.findByUserIdAndVolunteeringId(username, volunteeringId)){
            if(request.getStartTime().getTime() == start.getTime() && request.getEndTime().getTime() == end.getTime()){
                requestToApprove = request;
            }
        }
        if(requestToApprove == null){
            throw new UnsupportedOperationException("There is no hour approval request for user " + username + " in volunteering " + volunteeringId + " from " + start + " to " + end);
        }
        hourRequestJPA.delete(requestToApprove);
    }

    @Override
    public int getAmountOfAppointmentsInRestrict(int volunteeringId, int rangeId, RestrictionTuple r, boolean[] weekDays, LocalDate oneTime) {
        List<ScheduleAppointment> appointments = getVolunteeringAppointments(volunteeringId).stream().filter(appointment -> appointment.getRangeId() == rangeId).collect(Collectors.toList());
        int count = 0;
        for(ScheduleAppointment appointment : appointments){
            if(r.intersect(appointment.getStartTime(), appointment.getEndTime())){
                count++;
            }
        }
        return count;
    }

    @Override
    public void removeAppointmentsAndRequestsForVolunteering(int volunteeringId) {
        hourRequestJPA.deleteByVolunteeringId(volunteeringId);
        appointmentJPA.deleteByVolunteeringId(volunteeringId);
    }

    @Override
    public void removeAppointmentsOfRange(int volunteeringId, int rID) {
        appointmentJPA.deleteByVolunteeringIdAndRangeId(volunteeringId, rID);
    }
}
