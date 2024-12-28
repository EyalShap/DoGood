package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface SchedulingManager {
    public List<ScheduleAppointment> getUserAppointments(String username, List<Integer> volunteeringIds);
    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date start, Date end, int minutesAllowed);
    public ScheduleAppointment getUserAppointmentInRange(String username, int volunteeringId, Date include, int minutesAllowed);
    public List<HourApprovalRequests> getUserHourApproveRequests(String username, List<Integer> volunteeringIds);
    public List<ApprovedHours> getApprovedUserHours(String username, List<Integer> volunteeringIds);
    public List<ScheduleAppointment> getVolunteeringAppointments(int volunteeringId);
    public List<HourApprovalRequests> getVolunteeringHourApproveRequests(int volunteeringId);
    public void makeAppointment(ScheduleAppointment appointment);
    public void addHourApprovalRequest(String username, int volunteeringId, Date start, Date end);
    public void approveUserHours(String username, int volunteeringId, Date start, Date end);
    public void denyUserHours(String username, int volunteeringId, Date start, Date end);
    public int getAmountOfAppointmentsInRestrict(int volunteeringId, int rangeId, RestrictionTuple r, boolean[] weekDays, LocalDate oneTime);
}
