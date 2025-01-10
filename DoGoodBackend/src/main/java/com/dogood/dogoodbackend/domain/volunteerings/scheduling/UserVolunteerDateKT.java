package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import jakarta.persistence.Embeddable;

import java.time.LocalTime;
import java.util.Date;

@Embeddable
public class UserVolunteerDateKT {
    private String userId;
    private int volunteeringId;
    private Date startTime;

    public UserVolunteerDateKT(String userId, int volunteeringId, Date startTime) {
        this.userId = userId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
    }

    public UserVolunteerDateKT() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public void setVolunteeringId(int volunteeringId) {
        this.volunteeringId = volunteeringId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
