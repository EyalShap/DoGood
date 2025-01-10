package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

import java.util.Date;

@Entity
@IdClass(UserVolunteerDateKT.class)
public class ApprovedHours {
    @Id
    private String userId;

    @Id
    private int volunteeringId;

    @Id
    private Date startTime;
    private Date endTime;

    public ApprovedHours(String userId, int volunteeringId, Date startTime, Date endTime) {
        this.userId = userId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ApprovedHours() {}

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
