package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class MemorySchedulingManager implements SchedulingManager{
    private Map<Integer, Map<String, List<HourApprovalRequest>>> hourApprovalRequestsMapping;
    private Map<Integer, Map<String, List<ScheduleAppointment>>> appointmentsMapping;

    public MemorySchedulingManager() {
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

    private void checkExistsRequests(String username, int volunteeringId){
        if(!hourApprovalRequestsMapping.containsKey(volunteeringId)){
            throw new IllegalArgumentException("There is no mapping for volunteering with Id " + volunteeringId);
        }
        if(!hourApprovalRequestsMapping.get(volunteeringId).containsKey(username)){
            throw new IllegalArgumentException("There is no mapping for user " + username + " in volunteering " + volunteeringId);
        }
    }

    @Override
    public List<ScheduleAppointment> getAllAppointments() {
        List<ScheduleAppointment> all = new LinkedList<>();
        for(Map<String, List<ScheduleAppointment>> map : appointmentsMapping.values()){
            for(List<ScheduleAppointment> list : map.values()){
                all.addAll(list);
            }
        }
        return all;
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
    public List<HourApprovalRequest> getUserHourApproveRequests(String username, List<Integer> volunteeringIds) {
        List<HourApprovalRequest> requests = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            checkExistsRequests(username, volunteeringId);
            requests.addAll(hourApprovalRequestsMapping.get(volunteeringId).get(username));
        }
        return requests.stream().filter(request -> !request.isApproved()).toList();
    }

    @Override
    public List<HourApprovalRequest> getApprovedUserHours(String username, List<Integer> volunteeringIds) {
        List<HourApprovalRequest> approvedHours = new LinkedList<>();
        for(Integer volunteeringId : volunteeringIds) {
            checkExistsRequests(username, volunteeringId);
            approvedHours.addAll(hourApprovalRequestsMapping.get(volunteeringId).get(username));
        }
        return approvedHours.stream().filter(HourApprovalRequest::isApproved).toList();
    }

    @Override
    public List<HourApprovalRequest> getApprovedUserHours(String username) {
        List<HourApprovalRequest> approvedHours = new LinkedList<>();
        for(Integer volunteeringId : hourApprovalRequestsMapping.keySet()) {
            if(hourApprovalRequestsMapping.get(volunteeringId).containsKey(username)) {
                approvedHours.addAll(hourApprovalRequestsMapping.get(volunteeringId).get(username));
            }
        }
        return approvedHours.stream().filter(HourApprovalRequest::isApproved).toList();
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
    public List<HourApprovalRequest> getVolunteeringHourApproveRequests(int volunteeringId) {
        List<HourApprovalRequest> requests = new LinkedList<>();
        if(!hourApprovalRequestsMapping.containsKey(volunteeringId)){
            throw new IllegalArgumentException("There is no mapping for volunteering with Id " + volunteeringId);
        }
        for(String username : hourApprovalRequestsMapping.get(volunteeringId).keySet()) {
            checkExistsRequests(username, volunteeringId);
            requests.addAll(hourApprovalRequestsMapping.get(volunteeringId).get(username));
        }
        return requests.stream().filter(request -> !request.isApproved()).toList();
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
        if(end.before(start)){
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        for(HourApprovalRequest request : hourApprovalRequestsMapping.get(volunteeringId).get(username)){
            if(request.intersect(start, end)){
                throw new UnsupportedOperationException("A request by username " + username + " in this range already exists");
            }
        }
        hourApprovalRequestsMapping.get(volunteeringId).get(username).add(new HourApprovalRequest(username, volunteeringId, start, end));
    }

    @Override
    public void approveUserHours(String username, int volunteeringId, Date start, Date end) {
        checkExistsRequests(username, volunteeringId);
        HourApprovalRequest requestToApprove = null;
        for(HourApprovalRequest request : hourApprovalRequestsMapping.get(volunteeringId).get(username)){
            if(request.getStartTime().equals(start) && request.getEndTime().equals(end)){
                requestToApprove = request;
            }
        }
        if(requestToApprove == null){
            throw new UnsupportedOperationException("There is no hour approval request for user " + username + " in volunteering " + volunteeringId + " from " + start + " to " + end);
        }
        requestToApprove.approve();
    }

    @Override
    public void denyUserHours(String username, int volunteeringId, Date start, Date end) {
        checkExistsRequests(username, volunteeringId);
        HourApprovalRequest requestToDeny = null;
        for(HourApprovalRequest request : hourApprovalRequestsMapping.get(volunteeringId).get(username)){
            if(request.getStartTime().equals(start) && request.getEndTime().equals(end)){
                requestToDeny = request;
            }
        }
        if(requestToDeny == null){
            throw new UnsupportedOperationException("There is no hour approval request for user " + username + " in volunteering " + volunteeringId + " from " + start + " to " + end);
        }
        hourApprovalRequestsMapping.get(volunteeringId).get(username).remove(requestToDeny);
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
        if(hourApprovalRequestsMapping.containsKey(volunteeringId)){
            List<String> usernamesToRemove = new LinkedList<>();
            for(String username : hourApprovalRequestsMapping.get(volunteeringId).keySet()){
                hourApprovalRequestsMapping.get(volunteeringId).get(username).removeIf(request -> !request.isApproved());
                if(hourApprovalRequestsMapping.get(volunteeringId).get(username).isEmpty()){
                    usernamesToRemove.add(username);
                }
            }
            for(String username : usernamesToRemove){
                hourApprovalRequestsMapping.get(volunteeringId).remove(username);
            }
            if(hourApprovalRequestsMapping.get(volunteeringId).size() == 0){
                hourApprovalRequestsMapping.remove(volunteeringId);
            }
        }
        if(appointmentsMapping.containsKey(volunteeringId)){
            appointmentsMapping.remove(volunteeringId);
        }
    }

    @Override
    public void removeAppointmentsOfRange(int volunteeringId, int rID) {
        if(appointmentsMapping.containsKey(volunteeringId)){
            for(String userId : appointmentsMapping.get(volunteeringId).keySet()){
                appointmentsMapping.get(volunteeringId).get(userId).removeIf(appointment -> appointment.getRangeId() == rID);
            }
        }
    }

    @Override
    public void cancelAppointment(String userId, int volunteeringId, LocalTime start) {
        checkExistsAppointments(userId, volunteeringId);
        if(!appointmentsMapping.get(volunteeringId).get(userId).removeIf(appointment -> appointment.getStartTime().equals(start))){
            throw new IllegalArgumentException("There is no appointment for volunteering " + volunteeringId + " by user " + userId + " that starts at " + start);
        }
    }

    @Override
    public void userLeave(int volunteeringId, String userId) {
        if(hourApprovalRequestsMapping.containsKey(volunteeringId)){
            if(hourApprovalRequestsMapping.get(volunteeringId).containsKey(userId)){
                hourApprovalRequestsMapping.get(volunteeringId).get(userId).removeIf(request -> !request.isApproved());
                if(hourApprovalRequestsMapping.get(volunteeringId).get(userId).isEmpty()){
                    hourApprovalRequestsMapping.get(volunteeringId).remove(userId);
                }
            }
        }
        if(appointmentsMapping.containsKey(volunteeringId)){
            if(appointmentsMapping.get(volunteeringId).containsKey(userId)){
                appointmentsMapping.get(volunteeringId).remove(userId);
            }
        }
    }

    @Override
    public void updateRequestDescription(String userId, int volunteeringId, Date start, String newDescription) {
        if(hourApprovalRequestsMapping.containsKey(volunteeringId)){
            if(hourApprovalRequestsMapping.get(volunteeringId).containsKey(userId)){
                HourApprovalRequest requestToUpdate = null;
                for(HourApprovalRequest request : hourApprovalRequestsMapping.get(volunteeringId).get(userId)){
                    if(request.getStartTime().equals(start)){
                        requestToUpdate = request;
                    }
                }
                if(requestToUpdate == null){
                    throw new IllegalArgumentException("Specified Hour Request not found");
                }
                requestToUpdate.setDescription(newDescription);
            }
        }
    }

    @Override
    public void removeAppointmentsOfVolunteer(int volunteeringId, String volunteerId) {
        if(appointmentsMapping.containsKey(volunteeringId)){
            if(appointmentsMapping.get(volunteeringId).containsKey(volunteerId)){
                appointmentsMapping.get(volunteeringId).remove(volunteerId);
            }
        }
    }
}
