package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import jakarta.persistence.Embeddable;

import java.time.LocalTime;

@Embeddable
public class RestrictionTuple {
    private LocalTime startTime;
    private LocalTime endTime;
    private int amount;

    public RestrictionTuple(LocalTime startTime, LocalTime endTime, int amount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.amount = amount;
    }

    public RestrictionTuple() {}

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

    public RestrictionTuple intersection(LocalTime otherStartTime, LocalTime otherEndTime) {
        LocalTime latestStart = otherStartTime.isAfter(this.startTime) ? otherStartTime : this.startTime;
        LocalTime earliestEnd = otherEndTime.isBefore(this.endTime) ? otherEndTime : this.endTime;
        return new RestrictionTuple(latestStart, earliestEnd, amount);
    }
}
