package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class MemorySchedulingManager implements SchedulingManager{
    private Map<Integer, Map<String, List<ApprovedHours>>> approvedHoursMapping;
    private Map<Integer, Map<String, List<HourApprovalRequests>>> hourApprovalRequestsMapping;
    private Map<Integer, Map<String, List<ScheduleAppointment>>> appointmentsMapping;

    public MemorySchedulingManager() {
        approvedHoursMapping =new HashMap<>();
        hourApprovalRequestsMapping =new HashMap<>();
        appointmentsMapping =new HashMap<>();
    }

    private void checkExistsAppointments(String username, int volunteeringId){
        if(!appointmentsMapping.containsKey(volunteeringId)){
            throw new IllegalArgumentException("There is no mapping for volunteering with Id " + volunteeringId);
        }
        if(!appointmentsMapping.get(volunteeringId).containsKey(username)){
            throw new IllegalArgumentException("There is no mapping for user " + username + " in volunteering " + volunteeringId);
        }
    }

    private void checkExistsApprovals(String username, int volunteeringId){
        if(!approvedHoursMapping.containsKey(volunteeringId)){
            throw new IllegalArgumentException("There is no mapping for volunteering with Id " + volunteeringId);
        }
        if(!approvedHoursMapping.get(volunteeringId).containsKey(username)){
            throw new IllegalArgumentException("There is no mapping for user " + username + " in volunteering " + volunteeringId);
        }
    }

    private void checkExistsRequests(String username, int volunteeringId){
        if(!hourApprovalRequestsMapping.containsKey(volunteeringId)){
            throw new IllegalArgumentException("There is no mapping for volunteering with Id " + volunteeringId);
        }
        if(!hourApprovalRequestsMapping.get(volunteeringId).containsKey(username)){
            throw new IllegalArgumentException("There is no mapping for user " + username + " in volunteering " + volunteeringId);
        }
    }

    @Override
    public List<ScheduleAppointment> getUserAppointments(String username, List<Integer> volunteeringIds) {
        List<ScheduleAppointment> appointments = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            checkExistsAppointments(username, volunteeringId);
            appointments.addAll(appointmentsMapping.get(volunteeringId).get(username));
        }
        return appointments;
    }

    @Override
    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date start, Date end, int minutesAllowed) {
        checkExistsAppointments(username, volunteeringId);
        for(ScheduleAppointment appointment : appointmentsMapping.get(volunteeringId).get(username)) {
            if(appointment.matchRange(start, end, minutesAllowed)){
                return appointment;
            }
        }
        return null;
    }

    @Override
    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date include, int minutesAllowed) {
        checkExistsAppointments(username, volunteeringId);
        for(ScheduleAppointment appointment : appointmentsMapping.get(volunteeringId).get(username)) {
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
            checkExistsRequests(username, volunteeringId);
            requests.addAll(hourApprovalRequestsMapping.get(volunteeringId).get(username));
        }
        return requests;
    }

    @Override
    public List<ApprovedHours> getApprovedUserHours(String username, List<Integer> volunteeringIds) {
        List<ApprovedHours> approvedHours = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            checkExistsApprovals(username, volunteeringId);
            approvedHours.addAll(approvedHoursMapping.get(volunteeringId).get(username));
        }
        return approvedHours;
    }

    @Override
    public List<ScheduleAppointment> getVolunteeringAppointments(int volunteeringId) {
        List<ScheduleAppointment> appointments = new LinkedList<>();
        if(!appointmentsMapping.containsKey(volunteeringId)){
            throw new IllegalArgumentException("There is no mapping for volunteering with Id " + volunteeringId);
        }
        for(String username : appointmentsMapping.get(volunteeringId).keySet()) {
            checkExistsAppointments(username, volunteeringId);
            appointments.addAll(appointmentsMapping.get(volunteeringId).get(username));
        }
        return appointments;
    }

    @Override
    public List<HourApprovalRequests> getVolunteeringHourApproveRequests(int volunteeringId) {
        List<HourApprovalRequests> requests = new LinkedList<>();
        if(!hourApprovalRequestsMapping.containsKey(volunteeringId)){
            throw new IllegalArgumentException("There is no mapping for volunteering with Id " + volunteeringId);
        }
        for(String username : hourApprovalRequestsMapping.get(volunteeringId).keySet()) {
            checkExistsRequests(username, volunteeringId);
            requests.addAll(hourApprovalRequestsMapping.get(volunteeringId).get(username));
        }
        return requests;
    }

    @Override
    public void makeAppointment(ScheduleAppointment appointment) {
        if(!appointmentsMapping.containsKey(appointment.getVolunteeringId())){
            appointmentsMapping.put(appointment.getVolunteeringId(), new HashMap<>());
        }
        if(!appointmentsMapping.get(appointment.getVolunteeringId()).containsKey(appointment.getUserId())){
            appointmentsMapping.get(appointment.getVolunteeringId()).put(appointment.getUserId(), new LinkedList<>());
        }
        for(ScheduleAppointment other : appointmentsMapping.get(appointment.getVolunteeringId()).get(appointment.getUserId())){
            if(appointment.intersect(other)){
                throw new IllegalArgumentException("You have an intersecting appointment within this range");
            }
        }
        appointmentsMapping.get(appointment.getVolunteeringId()).get(appointment.getUserId()).add(appointment);
    }

    @Override
    public void addHourApprovalRequest(String username, int volunteeringId, Date start, Date end) {
        if(!hourApprovalRequestsMapping.containsKey(volunteeringId)){
            hourApprovalRequestsMapping.put(volunteeringId, new HashMap<>());
        }
        if(!hourApprovalRequestsMapping.get(volunteeringId).containsKey(username)){
            hourApprovalRequestsMapping.get(volunteeringId).put(username, new LinkedList<>());
        }
        for(HourApprovalRequests request : hourApprovalRequestsMapping.get(volunteeringId).get(username)){
            if(request.intersect(start, end)){
                throw new UnsupportedOperationException("A request by username " + username + " in this range already exists");
            }
        }
        hourApprovalRequestsMapping.get(volunteeringId).get(username).add(new HourApprovalRequests(username, start, end));
    }

    private void addApproval(String username, int volunteeringId, Date start, Date end) {
        if(!approvedHoursMapping.containsKey(volunteeringId)){
            approvedHoursMapping.put(volunteeringId, new HashMap<>());
        }
        if(!approvedHoursMapping.get(volunteeringId).containsKey(username)){
            approvedHoursMapping.get(volunteeringId).put(username, new LinkedList<>());
        }

        approvedHoursMapping.get(volunteeringId).get(username).add(new ApprovedHours(username, volunteeringId, start, end));
    }

    @Override
    public void approveUserHours(String username, int volunteeringId, Date start, Date end) {
        checkExistsRequests(username, volunteeringId);
        HourApprovalRequests requestToApprove = null;
        for(HourApprovalRequests request : hourApprovalRequestsMapping.get(volunteeringId).get(username)){
            if(request.getStartTime().equals(start) && request.getEndTime().equals(end)){
                requestToApprove = request;
            }
        }
        if(requestToApprove == null){
            throw new UnsupportedOperationException("There is no hour approval request for user " + username + " in volunteering " + volunteeringId + " from " + start + " to " + end);
        }
        addApproval(username, volunteeringId, start, end);
        hourApprovalRequestsMapping.get(volunteeringId).get(username).remove(requestToApprove);
    }

    @Override
    public void denyUserHours(String username, int volunteeringId, Date start, Date end) {
        checkExistsRequests(username, volunteeringId);
        HourApprovalRequests requestToApprove = null;
        for(HourApprovalRequests request : hourApprovalRequestsMapping.get(volunteeringId).get(username)){
            if(request.getStartTime().equals(start) && request.getEndTime().equals(end)){
                requestToApprove = request;
            }
        }
        if(requestToApprove == null){
            throw new UnsupportedOperationException("There is no hour approval request for user " + username + " in volunteering " + volunteeringId + " from " + start + " to " + end);
        }
        hourApprovalRequestsMapping.get(volunteeringId).get(username).remove(requestToApprove);
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
}
