package com.dogood.dogoodbackend.domain.volunteerings.scheduling;

import com.dogood.dogoodbackend.domain.volunteerings.ScheduleRangeDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

public class ScheduleAppointmentDTO {
    private String userId;
    private int rangeId;
    private int volunteeringId;
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime startTime;
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime endTime;
    private boolean[] weekDays;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate oneTime;

    public ScheduleAppointmentDTO(String userId, int volunteeringId, int rangeId, LocalTime startTime, LocalTime endTime, boolean[] weekDays, LocalDate oneTime) {
        this.userId = userId;
        this.rangeId = rangeId;
        this.volunteeringId = volunteeringId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.weekDays = weekDays;
        this.oneTime = oneTime;
    }

    public String getUserId() {
        return userId;
    }

    public int getRangeId() {
        return rangeId;
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean[] getWeekDays() {
        return weekDays;
    }

    public LocalDate getOneTime() {
        return oneTime;
    }

    public String toCsv(String volunteeringName, int numOfWeeks) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        LocalDate now = LocalDate.now();
        if(weekDays == null){
            if(ChronoUnit.WEEKS.between(now, oneTime) > numOfWeeks){
                return "";
            }
            return volunteeringName + "," + dateTimeFormatter.format(oneTime) + "," + timeFormatter.format(startTime) + "," + timeFormatter.format(endTime) + "\n";
        }
        String csv = "";
        for(int i = 0; i < weekDays.length; i++){
            if(weekDays[i]){
                for(int j = 0; j < numOfWeeks; j++){
                    csv += volunteeringName + "," + dateTimeFormatter.format(now.with(TemporalAdjusters.next(DayOfWeek.of(i == 0 ? 7 : i))).plusWeeks(j)) + "," + timeFormatter.format(startTime) + "," + timeFormatter.format(endTime) + "\n";
                }
            }
        }
        return csv;
    }
}
