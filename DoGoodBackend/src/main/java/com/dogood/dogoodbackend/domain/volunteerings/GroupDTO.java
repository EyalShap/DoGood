package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GroupDTO {
    private final int id;
    private List<String> users;

    private Map<String, Integer> volunteersToLocation;
    private Map<Integer, List<Integer>> locationToRanges;

    public GroupDTO(int id, List<String> users, Map<String, Integer> volunteersToLocation, Map<Integer, List<Integer>> locationToRanges) {
        this.id = id;
        this.users = new LinkedList<>();
        this.volunteersToLocation = volunteersToLocation;
        this.locationToRanges = locationToRanges;
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

    public Map<Integer, List<Integer>> getLocationToRanges() {
        return locationToRanges;
    }
}
