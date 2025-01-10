package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import jakarta.persistence.Embeddable;

import java.time.LocalTime;

@Embeddable
public class UserVolunteerTimeKT {
    private String userId;
    private int volunteeringId;
    private LocalTime startTime;

    public UserVolunteerTimeKT(String userId, int volunteeringId, LocalTime startTime) {
        this.userId = userId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
    }

    public UserVolunteerTimeKT() {

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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
