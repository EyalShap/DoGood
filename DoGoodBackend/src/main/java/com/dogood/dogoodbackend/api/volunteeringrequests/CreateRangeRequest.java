package com.dogood.dogoodbackend.api.volunteeringrequests;

import java.time.LocalDate;

public class CreateRangeRequest {
    private int volunteeringId;
    private int groupId;
    private int locId;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private int minimumMinutes;
    private int maximumMinutes;
    private boolean[] weekDays;
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

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getMinimumMinutes() {
        return minimumMinutes;
    }

    public void setMinimumMinutes(int minimumMinutes) {
        this.minimumMinutes = minimumMinutes;
    }

    public int getMaximumMinutes() {
        return maximumMinutes;
    }

    public void setMaximumMinutes(int maximumMinutes) {
        this.maximumMinutes = maximumMinutes;
    }

    public boolean[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(boolean[] weekDays) {
        this.weekDays = weekDays;
    }

    public LocalDate getOneTime() {
        return oneTime;
    }

    public void setOneTime(LocalDate oneTime) {
        this.oneTime = oneTime;
    }
}
