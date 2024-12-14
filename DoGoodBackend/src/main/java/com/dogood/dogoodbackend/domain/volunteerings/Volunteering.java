package com.dogood.dogoodbackend.domain.volunteerings;

import java.util.*;

public class Volunteering {
    private final int id;
    private int availableGroupId;
    private int availableLocationId;
    private int organizationId;
    private String name;
    private String description;
    private List<String> skills;
    private List<String> categories;
    private Map<Integer,Location> locations;
    private Map<Integer, Group> groups;
    private List<PastExperience> pastExperiences;

    private Map<String,Integer> volunteerToGroup;

    private List<JoinRequest> pendingJoinRequests;
    private List<HourApprovalRequests> pendingHourApprovalRequests;

    public Volunteering(int id, int organizationId, String name, String description) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        this.availableGroupId = 0;
        this.availableLocationId = 0;
        this.groups = new HashMap<>();
        this.locations = new HashMap<>();
        this.pastExperiences = new LinkedList<>();
        this.volunteerToGroup = new HashMap<>();
        this.pendingJoinRequests = new LinkedList<>();
        this.pendingHourApprovalRequests = new LinkedList<>();

        addNewGroup();
    }

    public int getId() {
        return id;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSkills() {
        return skills;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<PastExperience> getPastExperiences() {
        return pastExperiences;
    }

    public Map<String, Integer> getVolunteerToGroup() {
        return volunteerToGroup;
    }

    public List<JoinRequest> getPendingJoinRequests() {
        return pendingJoinRequests;
    }

    public List<HourApprovalRequests> getPendingHourApprovalRequests() {
        return pendingHourApprovalRequests;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void addNewExperience(PastExperience e){
        this.pastExperiences.add(e);
    }

    public void addJoinRequest(JoinRequest r){
        this.pendingJoinRequests.add(r);
    }

    public void addHourRequest(HourApprovalRequests r){
        this.pendingHourApprovalRequests.add(r);
    }

    public void addNewGroup(){
        this.groups.put(availableGroupId, new Group(availableGroupId++));
    }

    public void removeGroup(int id){
        Group g = groups.get(id);
        if(!g.isEmpty()){
            throw new UnsupportedOperationException("Cannot remove a non-empty group, please re-assign the volunteers first");
        }
        groups.remove(id);
    }

    public void addLocation(String name, String address){
        this.locations.put(availableLocationId, new Location(availableGroupId++,name,address));
    }

    public void removeLocation(int id){
        this.locations.remove(id);
        for(Group g : groups.values()){
            g.removeLocationIfhas(id);
        }
    }
}
