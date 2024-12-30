package com.dogood.dogoodbackend.api.volunteeringrequests;

public class UpdateRangeWeekdaysRequest {
    private int volunteeringId;
    private int groupId;
    private int locId;
    private int rangeId;
    private boolean[] weekdays;

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

    public boolean[] getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(boolean[] weekdays) {
        this.weekdays = weekdays;
    }
}
