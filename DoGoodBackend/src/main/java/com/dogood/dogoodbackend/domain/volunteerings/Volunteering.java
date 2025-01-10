package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointmentDTO;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Volunteering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private transient int availableGroupId;
    private transient int availableLocationId;
    private transient int availableRangeId;
    private int organizationId;
    private String name;
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> categories;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> imagePaths;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteeringId")
    @MapKey(name="id")
    private Map<Integer,Location> locations;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name="volunteering_group_mapping", joinColumns = {@JoinColumn(name="vol_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "group_id", referencedColumnName = "id")})
    @MapKeyJoinColumn(name = "group_id")
    private Map<Integer, Group> groups;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<PastExperience> pastExperiences;

    private transient Map<String,Integer> volunteerToGroup;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="volunteering_joinrequests_mapping")
    @MapKeyJoinColumn(name = "user_id")
    private Map<String, JoinRequest> pendingJoinRequests;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteeringId")
    @MapKey(name="id")
    private Map<Integer, ScheduleRange> scheduleRanges;

    private ScanTypes scanTypes;
    private ApprovalType approvalType;

    private BarcodeHandler barcodeHandler;

    public Volunteering(int id, int organizationId, String name, String description, BarcodeHandler barcodeHandler) {
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
        this.barcodeHandler = barcodeHandler;
        this.imagePaths = new LinkedList<>();
        this.scheduleRanges = new HashMap<>();
        addNewGroup();
    }

    public Volunteering(int organizationId, String name, String description, BarcodeHandler barcodeHandler) {
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
        this.barcodeHandler = barcodeHandler;
        this.imagePaths = new LinkedList<>();
        this.scheduleRanges = new HashMap<>();
        addNewGroup();
    }

    public Volunteering() {

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

    private void addNewExperience(PastExperience e){
        this.pastExperiences.add(e);
    }

    public void addJoinRequest(String userId, JoinRequest r){
        if(pendingJoinRequests.containsKey(userId)){
            throw new UnsupportedOperationException("There is already a join request for this user!");
        }
        this.pendingJoinRequests.put(userId,r);
    }

    public int addNewGroup(){
        Group g = new Group(availableGroupId++, id);
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

    public int addLocation(String name, AddressTuple address){
        Location loc = new Location(availableLocationId++,id,name,address);
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

    public String generateCode(boolean constant){
        String code;
        if(constant){
            code = barcodeHandler.generateConstantCode();
        }else{
            code = barcodeHandler.generateCode();
        }
        return "" + id + ":"+code;
    }

    public void clearConstantCodes(){
        this.barcodeHandler.clearConstantCodes();
    }

    public List<String> getConstantCodes(){
        return barcodeHandler.getConstantCodes().stream().map(c -> c.getCode()).collect(Collectors.toList());
    }

    public void approveJoinRequest(String userId, int groupId){
        if(!groups.containsKey(groupId)){
            throw new UnsupportedOperationException("There is no group with id "+groupId);
        }
        if(!pendingJoinRequests.containsKey(userId)){
            throw new UnsupportedOperationException("There is no pending join request for user "+userId);
        }
        pendingJoinRequests.remove(userId);
        groups.get(groupId).addUser(userId);
        volunteerToGroup.put(userId,groupId);
    }

    public void denyJoinRequest(String userId){
        if(!pendingJoinRequests.containsKey(userId)){
            throw new UnsupportedOperationException("There is no pending join request for user "+userId);
        }
        pendingJoinRequests.remove(userId);
    }

    public void leaveVolunteering(String userId, PastExperience e){
        if(!hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + id);
        }
        int groupId = volunteerToGroup.get(userId);
        Group group = groups.get(groupId);
        group.removeUser(userId);
        volunteerToGroup.remove(userId);
        addNewExperience(e);
    }

    public void moveVolunteerToNewGroup(String userId, int groupIdTo){
        if(!groups.containsKey(groupIdTo)){
            throw new UnsupportedOperationException("There is no group with id "+groupIdTo);
        }
        if(!hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + id);
        }
        int currentGroupId = volunteerToGroup.get(userId);
        Group groupFrom = groups.get(currentGroupId);
        Group groupTo = groups.get(groupIdTo);
        groupFrom.removeUser(userId);
        groupTo.addUser(userId);
    }

    public void assignVolunteerToLocation(String userId, int locId){
        if(!locations.containsKey(locId)){
            throw new UnsupportedOperationException("There is no location with id "+locId);
        }
        if(!hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + id);
        }
        int currentGroupId = volunteerToGroup.get(userId);
        Group group = groups.get(currentGroupId);
        group.assignUserToLocation(userId, locId);
    }

    public int addRangeToGroup(int groupId, int locId, LocalTime startTime, LocalTime endTime,int minimumAppointmentMinutes, int maximumAppointmentMinutes){
        if(startTime.isAfter(endTime)){
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        if(minimumAppointmentMinutes > 0 && minimumAppointmentMinutes > 0 && minimumAppointmentMinutes > maximumAppointmentMinutes){
            throw new IllegalArgumentException("Illegal appointment minutes range");
        }
        Group g = groups.get(groupId);
        ScheduleRange range = new ScheduleRange(availableRangeId++, id, startTime, endTime, minimumAppointmentMinutes, maximumAppointmentMinutes);
        scheduleRanges.put(range.getId(), range);
        g.addScheduleToLocation(locId, range.getId());
        return range.getId();
    }

    public void updateRangeWeekdays(int groupId, int locId, int rangeId, boolean[] weekdays){
        if(!scheduleRanges.containsKey(rangeId)){
            throw new IllegalArgumentException("No range with Id " + rangeId);
        }
        ScheduleRange range = scheduleRanges.get(rangeId);
        range.setWeekDays(weekdays);
    }

    public void updateRangeOneTimeDate(int groupId, int locId, int rangeId, LocalDate oneTime){
        if(!scheduleRanges.containsKey(rangeId)){
            throw new IllegalArgumentException("No range with Id " + rangeId);
        }
        ScheduleRange range = scheduleRanges.get(rangeId);
        range.setOneTime(oneTime);
    }

    public void addRestrictionToRange(int groupId, int locId, int rangeId, RestrictionTuple restriction){
        if(!groups.containsKey(groupId)){
            throw new UnsupportedOperationException("There is no group with id "+groupId);
        }
        if(!scheduleRanges.containsKey(rangeId)){
            throw new IllegalArgumentException("No range with Id " + rangeId);
        }
        scheduleRanges.get(rangeId).addRestriction(restriction);
    }

    public void removeRestrictionFromRange(int groupId, int locId, int rangeId, LocalTime startTime){
        if(!groups.containsKey(groupId)){
            throw new UnsupportedOperationException("There is no group with id "+groupId);
        }
        if(!scheduleRanges.containsKey(rangeId)){
            throw new IllegalArgumentException("No range with Id " + rangeId);
        }
        scheduleRanges.get(rangeId).removeRestrictionByStart(startTime);
    }

    public boolean hasVolunteer(String userId){
        return volunteerToGroup.containsKey(userId);
    }

    public VolunteeringDTO getDTO(){
        return new VolunteeringDTO(id, organizationId, name, description,
                skills == null ? skills : new LinkedList<>(skills),
                categories == null ? categories : new LinkedList<>(categories), new LinkedList<>(imagePaths));
    }

    public void addImagePath(String imagePath){
        imagePaths.add(imagePath);
    }

    public void removeImagePath(String imagePath){
        imagePaths.remove(imagePath);
    }

    public ScheduleRange getScheduleRange(int groupId, int locId, int rangeId){
        if(!groups.containsKey(groupId)){
            throw new UnsupportedOperationException("There is no group with id "+groupId);
        }
        if(!scheduleRanges.containsKey(rangeId)){
            throw new IllegalArgumentException("No range with Id " + rangeId);
        }
        return scheduleRanges.get(rangeId);
    }

    public List<LocationDTO> getLocationDTOs(){
        return locations.values().stream().map(location -> location.getDTO()).collect(Collectors.toList());
    }

    public GroupDTO getGroupDTO(int groupId){
        if(!groups.containsKey(groupId)){
            throw new UnsupportedOperationException("There is no group with id "+groupId);
        }
        Group g = groups.get(groupId);
        return g.getDTO();
    }

    public List<ScheduleRangeDTO> getVolunteerAvailableRanges(String userId){
        Group g = groups.get(volunteerToGroup.get(userId));
        return g.getRangesForUser(userId).stream().map(rangeId -> scheduleRanges.get(rangeId).getDTO()).toList();
    }

    public int getAssignedLocation(String volunteerId){
        if(!hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + id);
        }
        Group g = groups.get(volunteerToGroup.get(volunteerId));
        return g.getAssignedLocation(volunteerId);
    }

    @PostLoad
    private void load(){
        volunteerToGroup = new HashMap<>();
        for(Group g : groups.values()){
            for(String userId : g.getUsers()){
                volunteerToGroup.put(userId, g.getId());
            }
        }
        availableGroupId = 0;
        availableLocationId = 0;
        availableRangeId = 0;
        for(int groupId : groups.keySet()){
            if(groupId > availableGroupId){
                availableGroupId = groupId;
            }
        }

        for(int locId : locations.keySet()){
            if(locId > availableLocationId){
                availableLocationId = locId;
            }
        }

        for(int rangeId : scheduleRanges.keySet()){
            if(rangeId > availableRangeId){
                availableRangeId = rangeId;
            }
        }
    }

    public Map<Integer, ScheduleRange> getScheduleRanges() {
        return scheduleRanges;
    }
}
