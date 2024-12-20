package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Group {
    private final int id;
    private List<String> users;

    private Map<String, Integer> volunteersToLocation;
    private Map<Integer, List<ScheduleRange>> locationToRanges;

    public Group(int id) {
        this.id = id;
        this.users = new LinkedList<>();
        this.volunteersToLocation = new HashMap<>();
    }

    public boolean isEmpty(){
        return users.isEmpty();
    }

    public void addUser(String user){
        if(users.contains(user)){
            throw new IllegalArgumentException("User already exists");
        }
        users.add(user);
    }

    public void removeUser(String user){
        if(!users.contains(user)){
            throw new IllegalArgumentException("User not in group");
        }
        users.remove(user);
        volunteersToLocation.remove(user);
    }

    public void removeLocationIfhas(int locId){
        if(locationToRanges.containsKey(locId)){
            locationToRanges.remove(locId);
        }
        volunteersToLocation.entrySet().removeIf(entry -> entry.getValue() == locId);
    }

    public int getId() {
        return id;
    }

    public List<String> getUsers() {
        return users;
    }

    public Map<String, Integer> getVolunteersToLocation() {
        return volunteersToLocation;
    }

    public Map<Integer, List<ScheduleRange>> getLocationToRanges() {
        return locationToRanges;
    }

    public ScheduleRange getScheduleRange(int locId, int rangeId){
        if(!locationToRanges.containsKey(locId)){
            throw new IllegalArgumentException("No schedule range on location with id " +locId);
        }
        List<ScheduleRange> ranges = locationToRanges.get(locId);
        for(ScheduleRange range : ranges){
            if(range.getId() == rangeId){
                return range;
            }
        }
        throw new IllegalArgumentException("No schedule range with id "+rangeId);
    }

    public void addScheduleToLocation(int locId, ScheduleRange scheduleRange){
        if(!locationToRanges.containsKey(locId)){
            locationToRanges.put(locId, new LinkedList<>());
        }
        locationToRanges.get(locId).add(scheduleRange);
    }

    public void addRestrictionToRange(int locId, int rangeId, RestrictionTuple restriction){
        ScheduleRange range = getScheduleRange(locId, rangeId);
        range.addRestriction(restriction);
    }

    public void removeRestrictionFromRange(int locId, int rangeId, LocalTime startTime){
        ScheduleRange range = getScheduleRange(locId, rangeId);
        range.removeRestrictionByStart(startTime);
    }

    public void assignUserToLocation(String userId, int locId){
        if(!users.contains(userId)){
            throw new IllegalArgumentException("User not in group");
        }
        volunteersToLocation.put(userId, locId);
    }

    public List<ScheduleRange> getRangesForUser(String userId){
        if(!users.contains(userId)){
            throw new IllegalArgumentException("User not in group");
        }
        if(!volunteersToLocation.containsKey(userId)){
            throw new IllegalArgumentException("User was not assigned a location");
        }
        return locationToRanges.get(volunteersToLocation.get(userId));
    }
}
