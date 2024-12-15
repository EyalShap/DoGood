package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import com.dogood.dogoodbackend.domain.volunteerings.HourApprovalRequests;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface SchedulingManager {
    public List<ScheduleAppointment> getUserAppointments(String username, List<Integer> volunteeringIds);
    public List<ScheduleAppointment> getApprovedUserHours(String username, List<Integer> volunteeringIds);
    public List<ScheduleAppointment> getVolunteeringAppointments(int volunteeringId);
    public void makeAppointment(ScheduleAppointment appointment);
    public void approveAppointment(String username, int volunteeringId, HourApprovalRequests hr);
    public int amountAppointmentsInRange(int volunteeringId, int rangeId, LocalTime start, LocalTime end);
}
