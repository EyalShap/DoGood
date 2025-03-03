package com.dogood.dogoodbackend.service;


import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static java.lang.Thread.sleep;

@Service
public class VolunteeringService {

    private FacadeManager facadeManager;

    @Autowired
    public VolunteeringService(FacadeManager facadeManager){
        this.facadeManager = facadeManager;

/*
        //frontend testing scenarios
        int orgid = facadeManager.getOrganizationsFacade().createOrganization("OrgOrg",
                "i dont know what to write here this will never be relevant for me",
                "052-0520520",
                "irefuse@this.is.irelevant",
                "TheDoctor");
        int volId = facadeManager.getOrganizationsFacade().createVolunteering(orgid,"Clearing The Backrooms Together",
                "The Backrooms of 72 are mysterious areas, together we can clear them and help them become normal",
                "TheDoctor");
        facadeManager.getOrganizationsFacade().createVolunteering(orgid,"Construction of sleep pods",
                "mmmm sleep pods",
                "TheDoctor");
        facadeManager.getVolunteeringFacade().updateVolunteeringScanDetails("TheDoctor", volId, ScanTypes.DOUBLE_SCAN, ApprovalType.AUTO_FROM_SCAN);

        facadeManager.getVolunteeringFacade().requestToJoinVolunteering("EyalShapiro", volId, "plz i want join");
        facadeManager.getVolunteeringFacade().acceptUserJoinRequest("TheDoctor", volId, "EyalShapiro", 0);
        int locId = facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volId, "The Backrooms", new AddressTuple("B7", "Ben Gurion", "72"));
        int rID = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup("TheDoctor", volId, 0, locId, LocalTime.of(0,0), LocalTime.of(23,59), -1,-1);
        facadeManager.getVolunteeringFacade().updateRangeWeekdays("TheDoctor",volId, 0, locId, rID, new boolean[]{true,true,true,true,true,true,true});
        facadeManager.getVolunteeringFacade().assignVolunteerToLocation("TheDoctor", "EyalShapiro", volId, locId);
        facadeManager.getVolunteeringFacade().makeAppointment("EyalShapiro", volId, 0, locId, rID, LocalTime.of(9,0), LocalTime.of(11,0), null, LocalDate.of(2025, 1, 5));
        facadeManager.getVolunteeringFacade().makeAppointment("EyalShapiro", volId, 0, locId, rID, LocalTime.of(13,0), LocalTime.of(14,0), new boolean[]{false,true,false,false,false,true,false}, null);

        facadeManager.getVolunteeringFacade().requestHoursApproval("EyalShapiro", volId, Date.from(LocalDateTime.of(2025,1,6,12,0).atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDateTime.of(2025,1,6,14,0).atZone(ZoneId.systemDefault()).toInstant()));
        facadeManager.getVolunteeringFacade().requestHoursApproval("EyalShapiro", volId, Date.from(LocalDateTime.of(2025,1,6,10,0).atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(LocalDateTime.of(2025,1,6,12,0).atZone(ZoneId.systemDefault()).toInstant()));

        facadeManager.getPostsFacade().createVolunteeringPost("post1", "description1", "TheDoctor", volId);
        facadeManager.getVolunteeringFacade().requestToJoinVolunteering("DanaFriedman", 1, "plz i also want join");
        facadeManager.getVolunteeringFacade().acceptUserJoinRequest("TheDoctor", volId, "DanaFriedman", 0);
        int locId2 = facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volId, "The Poolrooms", new AddressTuple("B7", "Ben Gurion", "Sports Centre"));
        int rID2 = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup("TheDoctor", volId, 0, locId2, LocalTime.of(14,0), LocalTime.of(20,0), -1,120);
        facadeManager.getVolunteeringFacade().updateRangeWeekdays("TheDoctor",volId, 0, locId2, rID2, new boolean[]{false,true,false,false,true,true,false});
        facadeManager.getVolunteeringFacade().assignVolunteerToLocation("TheDoctor", "DanaFriedman", volId, locId2);
        int rID3 = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup("TheDoctor", volId, 0, locId2, LocalTime.of(18,0), LocalTime.of(20,0), 60,120);
        facadeManager.getVolunteeringFacade().updateRangeOneTimeDate("TheDoctor", volId, 0, locId2, rID3, LocalDate.of(2025, 1, 14));*/

        this.facadeManager.getUsersFacade().register("EyalManager", "123456", "Eyal Manager", "eyalm1000@gmail.com", "0528585519", new Date());
        this.facadeManager.getUsersFacade().register("DanaManager", "123456", "Dana Manager", "dafr@post.bgu.ac.il", "0520391312", new Date());

        int orgId = this.facadeManager.getOrganizationsFacade().createOrganization("Dana Corp", "Company of Dana", "0520391312", "dafr@post.bgu.ac.il", "DanaManager");
        int volId = this.facadeManager.getOrganizationsFacade().createVolunteering(orgId, "Training Puppies", "Help us train the puppies", "DanaManager");
        this.facadeManager.getVolunteeringFacade().addImageToVolunteering("DanaManager", volId, "https://s3.amazonaws.com/cdn-origin-etr.akc.org/wp-content/uploads/2022/07/20112512/American-Eskimo-Dog-puppy-running-outdoors.jpg");
        this.facadeManager.getPostsFacade().createVolunteeringPost("We are training puppies!", "Come join us to help train the puppies", "DanaManager", volId);
        this.facadeManager.getVolunteeringFacade().updateVolunteeringSkills("DanaManager", volId, List.of("Training", "Animal Care"));
        this.facadeManager.getVolunteeringFacade().updateVolunteeringCategories("DanaManager", volId, List.of("Animals", "Sports", "Puppies"));
    }

    private void checkToken(String token, String username){
        if(!facadeManager.getAuthFacade().getNameFromToken(token).equals(username)){
            throw new IllegalArgumentException("Invalid token");
        }
    }

    public Response<String> removeVolunteering(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeVolunteering(userId, volunteeringId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteering(String token, String userId, int volunteeringId, String name, String description){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateVolunteering(userId, volunteeringId, name, description);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringSkills(String token, String userId, int volunteeringId, List<String> skills){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateVolunteeringSkills(userId, volunteeringId, skills);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringCategories(String token, String userId, int volunteeringId, List<String> categories){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateVolunteeringCategories(userId, volunteeringId, categories);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringScanDetails(String token, String userId, int volunteeringId, ScanTypes scanTypes, ApprovalType approvalType){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateVolunteeringScanDetails(userId, volunteeringId, scanTypes, approvalType);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> scanCode(String token, String userId, String code){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().scanCode(userId, code);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> makeVolunteeringCode(String token, String userId, int volunteeringId, boolean constant){
        try{
            checkToken(token, userId);
            String code = facadeManager.getVolunteeringFacade().makeVolunteeringCode(userId, volunteeringId, constant);
            return Response.createResponse(code, null);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> requestToJoinVolunteering(String token, String userId, int volunteeringId, String freeText){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().requestToJoinVolunteering(userId, volunteeringId, freeText);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> acceptUserJoinRequest(String token, String userId, int volunteeringId, String joinerId, int groupId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().acceptUserJoinRequest(userId, volunteeringId, joinerId, groupId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> denyUserJoinRequest(String token, String userId, int volunteeringId, String joinerId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().denyUserJoinRequest(userId, volunteeringId, joinerId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> finishVolunteering(String token, String userId, int volunteeringId, String experience){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().finishVolunteering(userId, volunteeringId, experience);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> addVolunteeringLocation(String token, String userId, int volunteeringId, String name, AddressTuple address){
        try{
            checkToken(token, userId);
            int locId = facadeManager.getVolunteeringFacade().addVolunteeringLocation(userId, volunteeringId, name, address);
            return Response.createResponse(locId);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> assignVolunteerToLocation(String token, String userId, String volunteerId, int volunteeringId, int locId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().assignVolunteerToLocation(userId, volunteerId, volunteeringId, locId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> moveVolunteerGroup(String token, String userId, String volunteerId, int volunteeringId, int groupId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().moveVolunteerGroup(userId, volunteerId, volunteeringId, groupId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> createNewGroup(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            int groupId = facadeManager.getVolunteeringFacade().createNewGroup(userId, volunteeringId);
            return Response.createResponse(groupId);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeRange(String token, String userId, int volunteeringId, int rID){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeRange(userId, volunteeringId, rID);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeGroup(String token, String userId, int volunteeringId, int groupId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeGroup(userId, volunteeringId, groupId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeLocation(String token, String userId, int volunteeringId, int locId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeLocation(userId, volunteeringId, locId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> addScheduleRangeToGroup(String token, String userId, int volunteeringId, int groupId, int locId, int startHour, int startMinute, int endHour, int endMinute, int minimumMinutes, int maximumMinutes, boolean[] weekDays, LocalDate oneTime){
        try{
            checkToken(token, userId);
            int rangeId = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup(userId, volunteeringId, groupId, locId, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), minimumMinutes, maximumMinutes, weekDays, oneTime);
            return Response.createResponse(rangeId);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> addRestrictionToRange(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, int startHour, int startMinute, int endHour, int endMinute, int amount){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().addRestrictionToRange(userId, volunteeringId, groupId, locId, rangeId, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), amount);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeRestrictionFromRange(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, int startHour, int startMinute){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeRestrictionFromRange(userId, volunteeringId, groupId, locId, rangeId, LocalTime.of(startHour, startMinute));
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateRangeWeekdays(String token, String userId, int volunteeringId, int rangeId, boolean[] weekdays){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateRangeWeekdays(userId, volunteeringId, rangeId, weekdays);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateRangeOneTimeDate(String token, String userId, int volunteeringId, int rangeId, LocalDate oneTime){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().updateRangeOneTimeDate(userId, volunteeringId, rangeId, oneTime);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> makeAppointment(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, int startHour, int startMinute, int endHour, int endMinute, boolean[] weekdays, LocalDate oneTime){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().makeAppointment(userId, volunteeringId, groupId, locId, rangeId, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), weekdays, oneTime);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> cancelAppointment(String token, String userId, int volunteeringId, int startHour, int startMinute){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().cancelAppointment(userId, volunteeringId, LocalTime.of(startHour, startMinute));
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> clearConstantCodes(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().clearConstantCodes(userId, volunteeringId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> requestHoursApproval(String token, String userId, int volunteeringId, Date start, Date end){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().requestHoursApproval(userId, volunteeringId, start, end);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> approveUserHours(String token, String userId, int volunteeringId, String volunteerId, Date start, Date end){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().approveUserHours(userId, volunteeringId, volunteerId, start, end);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> denyUserHours(String token, String userId, int volunteeringId, String volunteerId, Date start, Date end){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().denyUserHours(userId, volunteeringId, volunteerId, start, end);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<VolunteeringDTO> getVolunteeringDTO(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            VolunteeringDTO dto = facadeManager.getVolunteeringFacade().getVolunteeringDTO(volunteeringId);
            return Response.createResponse(dto);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getVolunteeringSkills(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            List<String> skills = facadeManager.getVolunteeringFacade().getVolunteeringSkills(volunteeringId);
            return Response.createResponse(skills);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getVolunteeringCategories(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            List<String> categs = facadeManager.getVolunteeringFacade().getVolunteeringCategories(volunteeringId);
            return Response.createResponse(categs);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<LocationDTO>> getVolunteeringLocations(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            List<LocationDTO> dtos = facadeManager.getVolunteeringFacade().getVolunteeringLocations(volunteeringId);
            return Response.createResponse(dtos);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<LocationDTO>> getGroupLocations(String token, String userId, int volunteeringId, int groupId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            List<LocationDTO> dtos = facadeManager.getVolunteeringFacade().getGroupLocations(volunteeringId, groupId);
            return Response.createResponse(dtos);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<Integer>> getVolunteeringGroups(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            List<Integer> groups = facadeManager.getVolunteeringFacade().getVolunteeringGroups(volunteeringId);
            return Response.createResponse(groups);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Map<String,Integer>> getVolunteeringVolunteers(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            Map<String,Integer> map = facadeManager.getVolunteeringFacade().getVolunteeringVolunteers(volunteeringId);
            return Response.createResponse(map);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<GroupDTO> getGroupDTO(String token, String userId, int volunteeringId, int groupId){
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            GroupDTO dto = facadeManager.getVolunteeringFacade().getGroupDTO(volunteeringId, groupId);
            return Response.createResponse(dto);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getConstantCodes(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            List<String> codes = facadeManager.getVolunteeringFacade().getConstantCodes(userId, volunteeringId);
            return Response.createResponse(codes);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ScheduleAppointmentDTO>> getVolunteerAppointments(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteerAppointments(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ScheduleRangeDTO>> getVolunteerAvailableRanges(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteerAvailableRanges(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges(String token, String userId, int volunteeringId, int groupId, int locId){
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringLocationGroupRanges(userId, volunteeringId, groupId, locId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<HourApprovalRequest>> getVolunteeringHourRequests(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringHourRequests(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<JoinRequest>> getVolunteeringJoinRequests(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            return Response.createResponse(new LinkedList<>(facadeManager.getVolunteeringFacade().getVolunteeringJoinRequests(userId, volunteeringId).values()));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> getUserAssignedLocation(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getUserAssignedLocation(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> getVolunteerGroup(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteerGroup(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<LocationDTO> getUserAssignedLocationData(String token, String userId, int volunteeringId){
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getUserAssignedLocationData(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> userHasSettingsPermission(String token, String userId, int volunteeringId) {
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().userHasSettingsPermission(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ScanTypes> getVolunteeringScanType(String token, String userId, int volunteeringId) {
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringScanType(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<ApprovalType> getVolunteeringApprovalType(String token, String userId, int volunteeringId) {
        try{
            checkToken(token, userId);
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringApprovalType(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> addImage(String token, String userId, int volunteeringId, String imagePath) {
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().addImageToVolunteering(userId, volunteeringId, imagePath);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeImage(String token, String userId, int volunteeringId, String imagePath) {
        try{
            checkToken(token, userId);
            facadeManager.getVolunteeringFacade().removeImageFromVolunteering(userId, volunteeringId, imagePath);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }
}
