package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

import java.util.Date;


@Entity
@IdClass(UserVolunteerDateKT.class)
public class HourApprovalRequest {
    @Id
    private String userId;

    @Id
    private int volunteeringId;

    @Id
    private Date startTime;
    private Date endTime;
    private boolean approved;

    public HourApprovalRequest(String userId, int volunteeringId, Date startTime, Date endTime) {
        this.userId = userId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.approved = false;
    }

    public HourApprovalRequest() {}

    public boolean intersect(Date otherStartTime, Date otherEndTime) {
        return !(otherStartTime.equals(this.endTime) || otherEndTime.equals(this.startTime) || otherStartTime.after(this.endTime) || otherEndTime.before(this.startTime));
    }

    public double getTotalHours(){
        long secs = (this.endTime.getTime() - this.startTime.getTime())/ 1000;
        return ((double)secs / 3600);
    }

    public String getUserId() {
        return userId;
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void approve(){
        this.approved = true;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}
