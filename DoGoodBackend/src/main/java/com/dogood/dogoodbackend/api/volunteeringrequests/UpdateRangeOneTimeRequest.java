package com.dogood.dogoodbackend.api.volunteeringrequests;

import java.time.LocalDate;

public class UpdateRangeOneTimeRequest {
    private int volunteeringId;
    private int groupId;
    private int locId;
    private int rangeId;
    private LocalDate oneTime;

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public void setVolunteeringId(int volunteeringId) {
        this.volunteeringId = volunteeringId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getLocId() {
        return locId;
    }

    public void setLocId(int locId) {
        this.locId = locId;
    }

    public int getRangeId() {
        return rangeId;
    }

    public void setRangeId(int rangeId) {
        this.rangeId = rangeId;
    }

    public LocalDate getOneTime() {
        return oneTime;
    }

    public void setOneTime(LocalDate oneTime) {
        this.oneTime = oneTime;
    }
}
