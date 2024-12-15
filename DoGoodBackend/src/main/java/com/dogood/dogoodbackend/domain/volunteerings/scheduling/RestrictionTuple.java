package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import java.time.LocalTime;

public class RestrictionTuple {
    private LocalTime startTime;
    private LocalTime endTime;
    private int amount;

    public RestrictionTuple(LocalTime startTime, LocalTime endTime, int amount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.amount = amount;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getAmount() {
        return amount;
    }

    public boolean intersect(LocalTime otherStartTime, LocalTime otherEndTime) {
        return !(otherStartTime.isAfter(this.endTime) || otherEndTime.isBefore(this.startTime));
    }
}
