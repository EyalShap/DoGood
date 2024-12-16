package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.util.Date;

public class HourApprovalRequests {
    String userId;
    Date startTime;
    Date endTime;

    public HourApprovalRequests(String userId, Date startTime, Date endTime) {
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getUserId() {
        return userId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}
