package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface SchedulingManager {
    public List<ScheduleAppointment> getUserAppointments(String username, List<Integer> volunteeringIds);
    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date start, Date end, int minutesAllowed);
    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date include);
    public List<ScheduleAppointment> getUserHourApproveRequests(String username, List<Integer> volunteeringIds);
    public List<ScheduleAppointment> getApprovedUserHours(String username, List<Integer> volunteeringIds);
    public List<ScheduleAppointment> getVolunteeringAppointments(int volunteeringId);
    public List<ScheduleAppointment> getVolunteeringHourApproveRequests(int volunteeringId);
    public void makeAppointment(ScheduleAppointment appointment);
    public int amountAppointmentsInRange(int volunteeringId, int rangeId, LocalTime start, LocalTime end);
    public void addHourApprovalRequest(String username, int volunteeringId, Date start, Date end);
    public void approveUserHours(String username, int volunteeringId, Date start, Date end);
}
