package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleRange;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class Volunteering {
    private final int id;
    private int availableGroupId;
    private int availableLocationId;
    private int availableRangeId;
    private int organizationId;
    private String name;
    private String description;
    private List<String> skills;
    private List<String> categories;
    private Map<Integer,Location> locations;
    private Map<Integer, Group> groups;
    private List<PastExperience> pastExperiences;

    private Map<String,Integer> volunteerToGroup;

    private Map<String, JoinRequest> pendingJoinRequests;

    private ScanTypes scanTypes;
    private ApprovalType approvalType;

    private boolean active;
    private BarcodeHandler barcodeHandler;

    public Volunteering(int id, int organizationId, String name, String description) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        this.availableGroupId = 0;
        this.availableLocationId = 0;
        this.availableRangeId = 0;
        this.groups = new HashMap<>();
        this.locations = new HashMap<>();
        this.pastExperiences = new LinkedList<>();
        this.volunteerToGroup = new HashMap<>();
        this.pendingJoinRequests = new HashMap<>();
        this.scanTypes = ScanTypes.NO_SCAN;
        this.approvalType = ApprovalType.MANUAL;
        this.barcodeHandler = new BarcodeHandler();
        this.active = true;

        addNewGroup();
    }

    public ScanTypes getScanTypes() {
        return scanTypes;
    }

    public void setScanTypes(ScanTypes scanTypes) {
        this.scanTypes = scanTypes;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(ApprovalType approvalType) {
        this.approvalType = approvalType;
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

    public Map<Integer,Location> getLocations() {
        return locations;
    }

    public Map<Integer,Group> getGroups() {
        return groups;
    }

    public List<PastExperience> getPastExperiences() {
        return pastExperiences;
    }

    public Map<String, Integer> getVolunteerToGroup() {
        return volunteerToGroup;
    }

    public Map<String,JoinRequest> getPendingJoinRequests() {
        return pendingJoinRequests;
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

    public void addJoinRequest(String userId, JoinRequest r){
        if(pendingJoinRequests.containsKey(userId)){
            throw new UnsupportedOperationException("There is already a join request for this user!");
        }
        this.pendingJoinRequests.put(userId,r);
    }

    public int addNewGroup(){
        Group g = new Group(availableGroupId++);
        this.groups.put(g.getId(), g);
        return g.getId();
    }

    public void removeGroup(int id){
        Group g = groups.get(id);
        if(!g.isEmpty()){
            throw new UnsupportedOperationException("Cannot remove a non-empty group, please re-assign the volunteers first");
        }
        groups.remove(id);
    }

    public int addLocation(String name, String address){
        Location loc = new Location(availableLocationId++,name,address);
        this.locations.put(loc.getId(), loc);
        return loc.getId();
    }

    public void removeLocation(int id){
        this.locations.remove(id);
        for(Group g : groups.values()){
            g.removeLocationIfhas(id);
        }
    }

    public boolean codeValid(String code){
        return barcodeHandler.codeValid(code);
    }

    public String generateCode(){
        return barcodeHandler.generateCode();
    }

    public String generateConstantCode(){
        return barcodeHandler.generateConstantCode();
    }

    public void clearConstantCodes(){
        this.barcodeHandler.clearConstantCodes();
    }

    public List<String> getConstantCodes(){
        return barcodeHandler.getConstantCodes().stream().map(c -> c.getCode()).collect(Collectors.toList());
    }

    public void toggleActive(){
        this.active = !this.active;
    }

    public void approveJoinRequest(String userId, int groupId){
        if(!groups.containsKey(groupId)){
            throw new UnsupportedOperationException("There is no group with id "+groupId);
        }
        if(!pendingJoinRequests.containsKey(userId)){
            throw new UnsupportedOperationException("There is no pending join request for user "+userId);
        }
        groups.get(groupId).addUser(userId);
    }

    public int addRangeToGroup(int groupId, int locId, LocalTime startTime, LocalTime endTime,int minimumAppointmentMinutes, int maximumAppointmentMinutes){
        Group g = groups.get(groupId);
        ScheduleRange range = new ScheduleRange(availableRangeId++, startTime, endTime, minimumAppointmentMinutes, maximumAppointmentMinutes);
        g.addScheduleToLocation(locId, range);
        return range.getId();
    }

    public void addRestrictionToRange(int groupId, int locId, int rangeId, RestrictionTuple restriction){
        Group g = groups.get(groupId);
        g.addRestrictionToRange(locId, rangeId, restriction);
    }

    public void removeRestrictionFromRange(int groupId, int locId, int rangeId, LocalTime startTime){
        Group g = groups.get(groupId);
        g.removeRestrictionFromRange(locId, rangeId, startTime);
    }

    public boolean hasVolunteer(String userId){
        return volunteerToGroup.containsKey(userId);
    }
}
