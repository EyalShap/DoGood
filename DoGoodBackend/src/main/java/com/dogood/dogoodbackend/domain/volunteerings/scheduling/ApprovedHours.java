package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.util.Date;

public class ApprovedHours {
    String userId;
    int volunteeringId;
    Date startTime;
    Date endTime;

    public ApprovedHours(String userId, int volunteeringId, Date startTime, Date endTime) {
        this.userId = userId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
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
