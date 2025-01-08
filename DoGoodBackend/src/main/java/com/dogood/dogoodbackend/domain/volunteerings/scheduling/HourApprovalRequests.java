package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalTime;
import java.util.Date;

public class HourApprovalRequests {
    private String userId;
    private int volunteeringId;
    private Date startTime;
    private Date endTime;

    public HourApprovalRequests(String userId, int volunteeringId, Date startTime, Date endTime) {
        this.userId = userId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean intersect(Date otherStartTime, Date otherEndTime) {
        return !(otherStartTime.equals(this.endTime) || otherEndTime.equals(this.startTime) || otherStartTime.after(this.endTime) || otherEndTime.before(this.startTime));
    }

    public String getUserId() {
        return userId;
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}
