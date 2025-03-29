package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@IdClass(IdVolunteeringPK.class)
@Table(name = "vgroup")
public class Group {
    @Id
    @Column(name="id")
    private int id;
    @Id
    @Column(name = "volunteering_id", nullable = false)
    private int volunteeringId;

    @ElementCollection
    private List<String> users;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="volunteer_location_mapping")
    @MapKeyColumn(name="user_id")
    @Column(name="loc_id")
    private Map<String, Integer> volunteersToLocation;

    @ElementCollection
    @CollectionTable(name="range_location_mapping")
    @MapKeyColumn(name="range_id")
    @Column(name="loc_id")
    private Map<Integer, Integer> rangeToLocation;

    public Group(int id, int volunteeringId) {
        this.id = id;
        this.volunteeringId = volunteeringId;
        this.users = new LinkedList<>();
        this.volunteersToLocation = new HashMap<>();
        this.rangeToLocation = new HashMap<>();
    }

    public Group() {

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

        volunteersToLocation.entrySet().removeIf(entry -> entry.getValue() == locId);
        rangeToLocation.entrySet().removeIf(entry -> entry.getValue() == locId);
    }

    public void removeRangeIfHas(int rangeId){
        if(rangeToLocation.containsKey(rangeId)){
            rangeToLocation.remove(rangeId);
        }
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

    public void addScheduleToLocation(int locId, int rangeId){
        rangeToLocation.put(rangeId, locId);
    }

    public void assignUserToLocation(String userId, int locId){
        if(!users.contains(userId)){
            throw new IllegalArgumentException("User not in group");
        }
        volunteersToLocation.put(userId, locId);
    }

    public List<Integer> getRangesForUser(String userId){
        if(!users.contains(userId)){
            throw new IllegalArgumentException("User not in group");
        }
        if(!volunteersToLocation.containsKey(userId)){
            throw new IllegalArgumentException("User was not assigned a location");
        }
        return rangeToLocation.keySet().stream().filter(rangeId -> rangeToLocation.get(rangeId)==volunteersToLocation.get(userId)).collect(Collectors.toList());
    }

    public GroupDTO getDTO(){
        Map<String, Integer> volunteersToLocationCopy = new HashMap<>();
        Map<Integer, List<Integer>> locationToRangesCopy = new HashMap<>();

        for(String userId : volunteersToLocation.keySet()){
            volunteersToLocationCopy.put(userId, volunteersToLocation.get(userId));
        }

        for(int rangeId : rangeToLocation.keySet()){
            if(!locationToRangesCopy.containsKey(rangeToLocation.get(rangeId))){
                locationToRangesCopy.put(rangeToLocation.get(rangeId), new LinkedList<>());
            }
            locationToRangesCopy.get(rangeToLocation.get(rangeId)).add(rangeId);
        }
        return new GroupDTO(id, new LinkedList<>(users), volunteersToLocationCopy, locationToRangesCopy);
    }

    public int getAssignedLocation(String volunteerId){
        if(!volunteersToLocation.containsKey(volunteerId)){
            return -2;
        }
        return volunteersToLocation.get(volunteerId);
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public Map<Integer, Integer> getRangeToLocation() {
        return rangeToLocation;
    }

    public void setVolunteeringId(int volunteeringId) {
        this.volunteeringId = volunteeringId;
    }

    public List<Integer> getRangesForLocation(int locId) {
        return rangeToLocation.keySet().stream().filter(rangeId -> rangeToLocation.get(rangeId)==locId).collect(Collectors.toList());
    }

    public boolean canMakeAppointments() {
        return !rangeToLocation.isEmpty();
    }

    public Map<Integer, List<Integer>> getLocationToRanges() {
        Map<Integer, List<Integer>> locationToRanges = new HashMap<>();
        for(int rangeId : rangeToLocation.keySet()){
            if(!locationToRanges.containsKey(rangeToLocation.get(rangeId))){
                locationToRanges.put(rangeToLocation.get(rangeId), new LinkedList<>());
            }
            locationToRanges.get(rangeToLocation.get(rangeId)).add(rangeId);
        }
        return locationToRanges;
    }
}
