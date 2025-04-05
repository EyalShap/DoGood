package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointmentDTO;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "volunteering_id", updatable = false)
    @MapKey(name="id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<Integer,Location> locations;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "volunteering_id", updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapKey(name="id")
    private Map<Integer, Group> groups;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<PastExperience> pastExperiences;

    private transient Map<String,Integer> volunteerToGroup;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="volunteering_joinrequests_mapping")
    @MapKeyJoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<String, JoinRequest> pendingJoinRequests;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "volunteering_id", updatable = false)
    @MapKey(name="id")
    @OnDelete(action = OnDeleteAction.CASCADE)
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
        if(groups.size() <= 1){
            throw new UnsupportedOperationException("Cannot have less than 1 group");
        }
        for(int rId : g.getRangeToLocation().keySet()){
            scheduleRanges.remove(rId);
        }
        groups.remove(id);
    }

    public void removeRange(int id){
        if(!scheduleRanges.containsKey(id)){
            throw new UnsupportedOperationException("There is no range with id "+id);
        }
        scheduleRanges.remove(id);
        for(Group g : groups.values()){
            g.removeRangeIfHas(id);
        }
    }

    public int addLocation(String name, AddressTuple address){
        Location loc = new Location(availableLocationId++,id,name,address);
        this.locations.put(loc.getId(), loc);
        return loc.getId();
    }

    public void removeLocation(int id){
        this.locations.remove(id);
        for(Group g : groups.values()){
            for(int rangeId : g.getRangesForLocation(id)){
                this.scheduleRanges.remove(rangeId);
            }
            g.removeLocationIfhas(id);
        }
    }

    public List<Integer> getRangeIdsForLocation(int id){
        List<Integer> rangeIds = new LinkedList<>();
        for(Group g : groups.values()){
            rangeIds.addAll(g.getRangesForLocation(id));
        }
        return rangeIds;
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
        return barcodeHandler.getConstantCodes().stream().map(c -> id + ":" + c.getCode()).collect(Collectors.toList());
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

    public synchronized void moveVolunteerToNewGroup(String userId, int groupIdTo){
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
        volunteerToGroup.put(userId, groupIdTo);
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

    public int addRangeToGroup(int groupId, int locId, LocalTime startTime, LocalTime endTime,int minimumAppointmentMinutes, int maximumAppointmentMinutes, boolean[] weekDays, LocalDate oneTime){
        if(!startTime.isBefore(endTime)){
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if(minimumAppointmentMinutes > 0 && maximumAppointmentMinutes > 0 && minimumAppointmentMinutes > maximumAppointmentMinutes){
            throw new IllegalArgumentException("Illegal appointment minutes range");
        }
        Group g = groups.get(groupId);
        ScheduleRange range = new ScheduleRange(availableRangeId++, id, startTime, endTime, minimumAppointmentMinutes, maximumAppointmentMinutes, weekDays, oneTime);
        scheduleRanges.put(range.getId(), range);
        g.addScheduleToLocation(locId, range.getId());
        return range.getId();
    }

    public void updateRangeOneTimeDate(int rangeId, LocalDate oneTime){
        scheduleRanges.get(rangeId).setOneTime(oneTime);
    }

    public void updateRangeWeekdays(int rangeId, boolean[] weekDays){
        scheduleRanges.get(rangeId).setWeekDays(weekDays);
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

    public List<LocationDTO> getGroupLocations(int groupId){
        if(!groups.containsKey(groupId)){
            throw new UnsupportedOperationException("There is no group with id "+groupId);
        }
        return groups.get(groupId).getLocationToRanges().keySet().stream().map(locId -> locations.get(locId).getDTO()).toList();
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
            return -2;
        }
        Group g = groups.get(volunteerToGroup.get(volunteerId));
        return g.getAssignedLocation(volunteerId);
    }

    public LocationDTO getAssignedLocationData(String volunteerId){
        if(!hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + id);
        }
        Group g = groups.get(volunteerToGroup.get(volunteerId));
        return locations.get(g.getAssignedLocation(volunteerId)).getDTO();
    }

    public int getVolunteerGroup(String volunteerId){
        if(!hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + id);
        }
        return volunteerToGroup.get(volunteerId);
    }

    public List<String> getWarnings(){
        List<String> warnings = new LinkedList<>();
        if(locations.isEmpty()){
            warnings.add("You don't have any locations defined. You can define locations or disable them in Settings.");
        }
        for(Group g : groups.values()){
            if(!g.canMakeAppointments()){
                warnings.add("There are no defined schedules for group " + g.getId() + ".");
            }
        }
        return warnings;
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
            if(groupId >= availableGroupId){
                availableGroupId = groupId+1;
            }
        }

        for(int locId : locations.keySet()){
            if(locId >= availableLocationId){
                availableLocationId = locId+1;
            }
        }

        for(int rangeId : scheduleRanges.keySet()){
            if(rangeId >= availableRangeId){
                availableRangeId = rangeId+1;
            }
        }
    }

    public Map<Integer, ScheduleRange> getScheduleRanges() {
        return scheduleRanges;
    }

    public List<ScheduleRangeDTO> getLocationGroupRanges(int groupId, int locId) {
        if(!groups.containsKey(groupId)){
            throw new UnsupportedOperationException("There is no group with id "+groupId);
        }
        if(!locations.containsKey(locId)){
            throw new UnsupportedOperationException("There is no location with id "+locId);
        }
        Group g = groups.get(groupId);
        List<Integer> rangeIds = g.getRangesForLocation(locId);
        return rangeIds.stream().map(rangeId -> scheduleRanges.get(rangeId).getDTO()).toList();
    }

    public List<String> getImagePaths() {
        return new LinkedList<>(imagePaths);
    }

    public void disableLocations() {
        if(!locations.isEmpty()){
            throw new UnsupportedOperationException("Cannot disable locations for a volunteering that has locations");
        }
        Location loc = new Location(-1,id,"Locations Disabled",null);
        this.locations.put(loc.getId(), loc);
    }
}
