package com.dogood.dogoodbackend.service;


import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.DatePair;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

@Service
public class VolunteeringService {

    private FacadeManager facadeManager;

    @Autowired
    public VolunteeringService(FacadeManager facadeManager){
        this.facadeManager = facadeManager;


        //frontend testing scenarios
        int orgid = facadeManager.getOrganizationsFacade().createOrganization("OrgOrg",
                "i dont know what to write here this will never be relevant for me",
                "052-0520520",
                "irefuse@this.is.irelevant",
                "TheDoctor");
        int volId = facadeManager.getOrganizationsFacade().createVolunteering(orgid,"Clearing The Backrooms Together",
                "The Backrooms of 72 are mysterious areas, together we can clear them and help them become normal",
                "TheDoctor");
        int volId2 = facadeManager.getOrganizationsFacade().createVolunteering(orgid,"Hiiiiiiiiiii",
                "blah blah blah blah blah",
                "TheDoctor");

        facadeManager.getVolunteeringFacade().requestToJoinVolunteering("EyalShapiro", volId, "plz i want join");
        facadeManager.getVolunteeringFacade().acceptUserJoinRequest("TheDoctor", volId, "EyalShapiro", 0);
        facadeManager.getOrganizationsFacade().sendAssignManagerRequest("EyalShapiro", "TheDoctor", orgid);

        facadeManager.getPostsFacade().createVolunteeringPost("post1", "dance", "TheDoctor", volId);
        facadeManager.getPostsFacade().createVolunteeringPost("post2", "dance", "TheDoctor", volId);
        facadeManager.getPostsFacade().createVolunteeringPost("post3", "description3", "TheDoctor", volId2);
        facadeManager.getVolunteeringFacade().updateVolunteeringSkills("TheDoctor", volId, List.of("First Aid"));
        facadeManager.getVolunteeringFacade().updateVolunteeringSkills("TheDoctor", volId2, List.of("Finance"));

        facadeManager.getVolunteeringFacade().updateVolunteeringCategories("TheDoctor", volId, List.of("Dance", "Art"));
        facadeManager.getVolunteeringFacade().updateVolunteeringCategories("TheDoctor", volId2, List.of("Dance", "Yoga"));

        facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volId, "What", new AddressTuple("Beer Sheva", "", ""));
        facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volId2, "What", new AddressTuple("Tel Aviv", "", ""));

        System.out.println("doneeeeeeeeeeeeeeeeee");
    }

    public Response<String> removeVolunteering(String token, String userId, int volunteeringId){
        try{
            facadeManager.getVolunteeringFacade().removeVolunteering(userId, volunteeringId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteering(String token, String userId, int volunteeringId, String name, String description){
        try{
            facadeManager.getVolunteeringFacade().updateVolunteering(userId, volunteeringId, name, description);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringSkills(String token, String userId, int volunteeringId, List<String> skills){
        try{
            facadeManager.getVolunteeringFacade().updateVolunteeringSkills(userId, volunteeringId, skills);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringCategories(String token, String userId, int volunteeringId, List<String> categories){
        try{
            facadeManager.getVolunteeringFacade().updateVolunteeringCategories(userId, volunteeringId, categories);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateVolunteeringScanDetails(String token, String userId, int volunteeringId, ScanTypes scanTypes, ApprovalType approvalType){
        try{
            facadeManager.getVolunteeringFacade().updateVolunteeringScanDetails(userId, volunteeringId, scanTypes, approvalType);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> scanCode(String token, String userId, String code){
        try{
            facadeManager.getVolunteeringFacade().scanCode(userId, code);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> makeVolunteeringCode(String token, String userId, int volunteeringId, boolean constant){
        try{
            String code = facadeManager.getVolunteeringFacade().makeVolunteeringCode(userId, volunteeringId, constant);
            return Response.createResponse(code, null);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> requestToJoinVolunteering(String token, String userId, int volunteeringId, String freeText){
        try{
            facadeManager.getVolunteeringFacade().requestToJoinVolunteering(userId, volunteeringId, freeText);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> acceptUserJoinRequest(String token, String userId, int volunteeringId, String joinerId, int groupId){
        try{
            facadeManager.getVolunteeringFacade().acceptUserJoinRequest(userId, volunteeringId, joinerId, groupId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> denyUserJoinRequest(String token, String userId, int volunteeringId, String joinerId){
        try{
            facadeManager.getVolunteeringFacade().denyUserJoinRequest(userId, volunteeringId, joinerId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> finishVolunteering(String token, String userId, int volunteeringId, String experience){
        try{
            facadeManager.getVolunteeringFacade().finishVolunteering(userId, volunteeringId, experience);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> addVolunteeringLocation(String token, String userId, int volunteeringId, String name, AddressTuple address){
        try{
            int locId = facadeManager.getVolunteeringFacade().addVolunteeringLocation(userId, volunteeringId, name, address);
            return Response.createResponse(locId);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> assignVolunteerToLocation(String token, String userId, String volunteerId, int volunteeringId, int locId){
        try{
            facadeManager.getVolunteeringFacade().assignVolunteerToLocation(userId, volunteerId, volunteeringId, locId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> moveVolunteerGroup(String token, String userId, String volunteerId, int volunteeringId, int groupId){
        try{
            facadeManager.getVolunteeringFacade().moveVolunteerGroup(userId, volunteerId, volunteeringId, groupId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> createNewGroup(String token, String userId, int volunteeringId){
        try{
            int groupId = facadeManager.getVolunteeringFacade().createNewGroup(userId, volunteeringId);
            return Response.createResponse(groupId);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeGroup(String token, String userId, int volunteeringId, int groupId){
        try{
            facadeManager.getVolunteeringFacade().removeGroup(userId, volunteeringId, groupId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeLocation(String token, String userId, int volunteeringId, int locId){
        try{
            facadeManager.getVolunteeringFacade().removeLocation(userId, volunteeringId, locId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> addScheduleRangeToGroup(String token, String userId, int volunteeringId, int groupId, int locId, int startHour, int startMinute, int endHour, int endMinute, int minimumMinutes, int maximumMinutes){
        try{
            int rangeId = facadeManager.getVolunteeringFacade().addScheduleRangeToGroup(userId, volunteeringId, groupId, locId, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), minimumMinutes, maximumMinutes);
            return Response.createResponse(rangeId);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateRangeWeekdays(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, boolean[] weekdays){
        try{
            facadeManager.getVolunteeringFacade().updateRangeWeekdays(userId, volunteeringId, groupId, locId, rangeId, weekdays);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateRangeOneTimeDate(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, LocalDate oneTime){
        try{
            facadeManager.getVolunteeringFacade().updateRangeOneTimeDate(userId, volunteeringId, groupId, locId, rangeId, oneTime);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> makeAppointment(String token, String userId, int volunteeringId, int groupId, int locId, int rangeId, int startHour, int startMinute, int endHour, int endMinute, boolean[] weekdays, LocalDate oneTime){
        try{
            facadeManager.getVolunteeringFacade().makeAppointment(userId, volunteeringId, groupId, locId, rangeId, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), weekdays, oneTime);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> clearConstantCodes(String token, String userId, int volunteeringId){
        try{
            facadeManager.getVolunteeringFacade().clearConstantCodes(userId, volunteeringId);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> requestHoursApproval(String token, String userId, int volunteeringId, Date start, Date end){
        try{
            facadeManager.getVolunteeringFacade().requestHoursApproval(userId, volunteeringId, start, end);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> approveUserHours(String token, String userId, int volunteeringId, String volunteerId, Date start, Date end){
        try{
            facadeManager.getVolunteeringFacade().approveUserHours(userId, volunteeringId, volunteerId, start, end);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> denyUserHours(String token, String userId, int volunteeringId, String volunteerId, Date start, Date end){
        try{
            facadeManager.getVolunteeringFacade().denyUserHours(userId, volunteeringId, volunteerId, start, end);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<VolunteeringDTO> getVolunteeringDTO(String token, String userId, int volunteeringId){
        try{
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            VolunteeringDTO dto = facadeManager.getVolunteeringFacade().getVolunteeringDTO(volunteeringId);
            return Response.createResponse(dto);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getVolunteeringSkills(String token, String userId, int volunteeringId){
        try{
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            List<String> skills = facadeManager.getVolunteeringFacade().getVolunteeringSkills(volunteeringId);
            return Response.createResponse(skills);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getVolunteeringCategories(String token, String userId, int volunteeringId){
        try{
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            List<String> categs = facadeManager.getVolunteeringFacade().getVolunteeringCategories(volunteeringId);
            return Response.createResponse(categs);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<LocationDTO>> getVolunteeringLocations(String token, String userId, int volunteeringId){
        try{
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            List<LocationDTO> dtos = facadeManager.getVolunteeringFacade().getVolunteeringLocations(volunteeringId);
            return Response.createResponse(dtos);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Map<String,Integer>> getVolunteeringVolunteers(String token, String userId, int volunteeringId){
        try{
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            Map<String,Integer> map = facadeManager.getVolunteeringFacade().getVolunteeringVolunteers(volunteeringId);
            return Response.createResponse(map);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<GroupDTO> getGroupDTO(String token, String userId, int volunteeringId, int groupId){
        try{
            facadeManager.getVolunteeringFacade().checkViewingPermissions(userId,volunteeringId);
            GroupDTO dto = facadeManager.getVolunteeringFacade().getGroupDTO(volunteeringId, groupId);
            return Response.createResponse(dto);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getConstantCodes(String token, String userId, int volunteeringId){
        try{
            List<String> codes = facadeManager.getVolunteeringFacade().getConstantCodes(userId, volunteeringId);
            return Response.createResponse(codes);
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }
}
