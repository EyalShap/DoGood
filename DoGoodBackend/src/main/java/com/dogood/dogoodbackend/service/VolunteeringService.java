package com.dogood.dogoodbackend.service;


import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.DatePair;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequests;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.RestrictionTuple;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
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
        int orgidmada = facadeManager.getOrganizationsFacade().createOrganization("Magen David Adom",
                "Magen David Adom (MDA) is Israel's national emergency medical service.",
                "052-0520520",
                "irefuse@this.is.irelevant",
                "TheDoctor");
        int volIdmada = facadeManager.getOrganizationsFacade().createVolunteering(orgidmada,"Ambulance driver",
                "A driver for our ambulances. A driver's licence is required.",
                "TheDoctor");
        int orgidlatet = facadeManager.getOrganizationsFacade().createOrganization("Latet",
                "Provides aid to .",
                "052-0520520",
                "irefuse@this.is.irelevant",
                "TheDoctor");
        int volIdletet = facadeManager.getOrganizationsFacade().createVolunteering(orgidlatet,"Packaging food",
                "Giving food package to families before Rosh Hashana.",
                "TheDoctor");

        facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volIdmada, "The Backrooms", new AddressTuple("Tel Aviv", "h", "h"));
        facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volIdletet, "The Backrooms", new AddressTuple("Beer Sheva", "h", "h"));
        facadeManager.getVolunteeringFacade().addVolunteeringLocation("TheDoctor", volIdletet, "The Backrooms", new AddressTuple("Jerusalem", "h", "h"));

        facadeManager.getVolunteeringFacade().updateVolunteeringCategories("TheDoctor", volIdmada, List.of("Health", "Emergency", "Drive"));
        facadeManager.getVolunteeringFacade().updateVolunteeringCategories("TheDoctor", volIdletet, List.of("Poverty", "Food"));
        facadeManager.getVolunteeringFacade().updateVolunteeringSkills("TheDoctor", volIdmada, List.of("Driving", "First Aid"));

        facadeManager.getPostsFacade().createVolunteeringPost("Ambulance driver", "A driver for our ambulances. A driver's licence is required.", "TheDoctor", volIdmada);
        facadeManager.getPostsFacade().createVolunteeringPost("Packaging food", "Giving food package to families before Rosh Hashana.", "TheDoctor", volIdletet);

        facadeManager.getVolunteeringFacade().requestToJoinVolunteering("Miryam", volIdmada, "jnkj");
        facadeManager.getVolunteeringFacade().acceptUserJoinRequest("TheDoctor", volIdmada, "Miryam", 0);
        facadeManager.getVolunteeringFacade().requestToJoinVolunteering("Shooky", volIdmada, "jnkj");
        facadeManager.getVolunteeringFacade().acceptUserJoinRequest("TheDoctor", volIdmada, "Shooky", 0);

        facadeManager.getVolunteeringFacade().finishVolunteering("Miryam", volIdmada, "I really enjoyed volunteering here. Felt meaningful.");
        facadeManager.getVolunteeringFacade().finishVolunteering("Shooky", volIdmada, "It was intense but important.");

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

    public Response<List<ScheduleAppointmentDTO>> getVolunteerAppointments(String token, String userId, int volunteeringId){
        try{
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteerAppointments(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<ScheduleRangeDTO>> getVolunteerAvailableRanges(String token, String userId, int volunteeringId){
        try{
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteerAvailableRanges(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<HourApprovalRequests>> getVolunteeringHourRequests(String token, String userId, int volunteeringId){
        try{
            return Response.createResponse(facadeManager.getVolunteeringFacade().getVolunteeringHourRequests(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<JoinRequest>> getVolunteeringJoinRequests(String token, String userId, int volunteeringId){
        try{
            return Response.createResponse(new LinkedList<>(facadeManager.getVolunteeringFacade().getVolunteeringJoinRequests(userId, volunteeringId).values()));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> getUserAssignedLocation(String token, String userId, int volunteeringId){
        try{
            return Response.createResponse(facadeManager.getVolunteeringFacade().getUserAssignedLocation(userId, volunteeringId));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }
}
