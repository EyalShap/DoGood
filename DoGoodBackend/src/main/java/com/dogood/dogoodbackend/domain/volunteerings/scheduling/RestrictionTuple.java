package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Embeddable;

import java.time.LocalTime;

@Embeddable
public class RestrictionTuple {
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime startTime;
    @JsonFormat(pattern="HH:mm:ss")
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

    private boolean afterOrEquals(LocalTime other){
        return other.isAfter(endTime) || other.equals(endTime);
    }

    private boolean beforeOrEquals(LocalTime other){
        return other.isBefore(startTime) || other.equals(startTime);
    }

    public boolean intersect(LocalTime otherStartTime, LocalTime otherEndTime) {
        return !(afterOrEquals(otherStartTime) || beforeOrEquals(otherEndTime));
    }

    public RestrictionTuple intersection(LocalTime otherStartTime, LocalTime otherEndTime) {
        LocalTime latestStart = otherStartTime.isAfter(this.startTime) ? otherStartTime : this.startTime;
        LocalTime earliestEnd = otherEndTime.isBefore(this.endTime) ? otherEndTime : this.endTime;
        return new RestrictionTuple(latestStart, earliestEnd, amount);
    }
}
