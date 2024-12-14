package com.dogood.dogoodbackend.domain.volunteerings;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Group {
    private final int id;
    private List<String> users;

    private Map<Integer, List<String>> locationToVolunteers;

    public Group(int id) {
        this.id = id;
        this.users = new LinkedList<>();
        this.locationToVolunteers = new HashMap<>();
    }

    public boolean isEmpty(){
        return users.isEmpty();
    }

    public void removeLocationIfhas(int locId){
        if(locationToVolunteers.containsKey(locId)){
            locationToVolunteers.remove(locId);
        }
    }
}
