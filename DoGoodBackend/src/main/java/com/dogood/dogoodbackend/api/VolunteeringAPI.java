package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.volunteeringrequests.*;
import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequests;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointmentDTO;
import com.dogood.dogoodbackend.service.Response;
import com.dogood.dogoodbackend.service.VolunteeringService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
@CrossOrigin
@RequestMapping("/api/volunteering")
public class VolunteeringAPI {
    @Autowired
    VolunteeringService volunteeringService; //this is also singleton

    @PatchMapping("/updateVolunteering")
    public Response<String> updateVolunteering(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody Map<String, String> body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.updateVolunteering(token, userId, volunteeringId, body.get("name"), body.get("description"));
    }

    @PatchMapping("/updateVolunteeringSkills")
    public Response<String> updateVolunteeringSkills(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody List<String> skills, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.updateVolunteeringSkills(token, userId, volunteeringId, skills);
    }

    @PatchMapping("/updateVolunteeringCategories")
    public Response<String> updateVolunteeringCategories(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody List<String> categories, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.updateVolunteeringCategories(token, userId, volunteeringId, categories);
    }

    @PatchMapping("/updateVolunteeringScanDetails")
    public Response<String> updateVolunteeringScanDetails(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody ScanDetailsRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.updateVolunteeringScanDetails(token, userId, volunteeringId, body.getScanTypes(), body.getApprovalType());
    }

    @PostMapping("/scanCode")
    public Response<String> scanCode(@RequestParam String userId, @RequestBody String code, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.scanCode(token, userId, code);
    }

    @PostMapping("/makeVolunteeringCode")
    public Response<String> makeVolunteeringCode(@RequestParam String userId, @RequestBody MakeCodeRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.makeVolunteeringCode(token, userId, body.getVolunteeringId(), body.isConstant());
    }

    @PostMapping("/requestToJoinVolunteering")
    public Response<String> requestToJoinVolunteering(@RequestParam String userId, @RequestBody IdAndTextRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.requestToJoinVolunteering(token, userId, body.getId(), body.getText());
    }

    @PutMapping("/acceptUserJoinRequest")
    public Response<String> acceptUserJoinRequest(@RequestParam String userId, @RequestBody JoinRequestHandleRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.acceptUserJoinRequest(token, userId, body.getVolunteeringId(), body.getRequesterId(), body.getGroupId());
    }

    @PutMapping("/denyUserJoinRequest")
    public Response<String> denyUserJoinRequest(@RequestParam String userId, @RequestBody JoinRequestHandleRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.denyUserJoinRequest(token, userId, body.getVolunteeringId(), body.getRequesterId());
    }

    @PostMapping("/finishVolunteering")
    public Response<String> finishVolunteering(@RequestParam String userId, @RequestBody IdAndTextRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.finishVolunteering(token, userId, body.getId(), body.getText());
    }

    @PostMapping("/addVolunteeringLocation")
    public Response<Integer> addVolunteeringLocation(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody CreateLocationRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.addVolunteeringLocation(token, userId, volunteeringId, body.getName(), body.getAddress());
    }

    @PatchMapping("/assignVolunteerToLocation")
    public Response<String> assignVolunteerToLocation(@RequestParam String userId, @RequestBody AssignVolunteerRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.assignVolunteerToLocation(token, userId, body.getVolunteerId(), body.getVolunteeringId(), body.getToId());
    }

    @PatchMapping("/moveVolunteerGroup")
    public Response<String> moveVolunteerGroup(@RequestParam String userId, @RequestBody AssignVolunteerRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.moveVolunteerGroup(token, userId, body.getVolunteerId(), body.getVolunteeringId(), body.getToId());
    }

    @PostMapping("/createNewGroup")
    public Response<Integer> createNewGroup(@RequestParam String userId, @RequestBody int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.createNewGroup(token, userId, volunteeringId);
    }

    @DeleteMapping("/removeGroup")
    public Response<String> removeGroup(@RequestParam String userId, @RequestBody Map<String, Integer> body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.removeGroup(token, userId, body.get("volunteeringId"), body.get("groupId"));
    }

    @DeleteMapping("/removeLocation")
    public Response<String> removeLocation(@RequestParam String userId, @RequestBody Map<String, Integer> body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.removeLocation(token, userId, body.get("volunteeringId"), body.get("locId"));
    }

    @PostMapping("/addScheduleRangeToGroup")
    public Response<Integer> addScheduleRangeToGroup(@RequestParam String userId, @RequestBody Map<String, Integer> body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.addScheduleRangeToGroup(token, userId,
                body.get("volunteeringId"),
                body.get("groupId"),
                body.get("locId"),
                body.get("startHour"),
                body.get("startMinute"),
                body.get("endHour"),
                body.get("endMinute"),
                body.get("minimumMinutes"),
                body.get("maximumMinutes"));
    }

    @PatchMapping("/updateRangeWeekdays")
    public Response<String> updateRangeWeekdays(@RequestParam String userId, @RequestBody UpdateRangeWeekdaysRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.updateRangeWeekdays(token, userId,
                body.getVolunteeringId(),
                body.getGroupId(),
                body.getLocId(),
                body.getRangeId(),
                body.getWeekdays());
    }


    @PatchMapping("/updateRangeOneTimeDate")
    public Response<String> updateRangeOneTimeDate(@RequestParam String userId, @RequestBody UpdateRangeOneTimeRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.updateRangeOneTimeDate(token, userId,
                body.getVolunteeringId(),
                body.getGroupId(),
                body.getLocId(),
                body.getRangeId(),
                body.getOneTime());
    }

    @PostMapping("/makeAppointment")
    public Response<String> makeAppointment(@RequestParam String userId, @RequestBody MakeAppointmentRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.makeAppointment(token, userId,
                body.getVolunteeringId(),
                body.getGroupId(),
                body.getLocId(),
                body.getRangeId(),
                body.getStartHour(),
                body.getStartMinute(),
                body.getEndHour(),
                body.getEndMinute(),
                body.getWeekdays(),
                body.getOneTime());
    }

    @DeleteMapping("/clearConstantCodes")
    public Response<String> clearConstantCodes(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.clearConstantCodes(token, userId,volunteeringId);
    }

    @PostMapping("/requestHoursApproval")
    public Response<String> requestHoursApproval(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody Map<String, Date> body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.requestHoursApproval(token, userId,volunteeringId, body.get("startDate"), body.get("endDate"));
    }

    @PostMapping("/approveUserHours")
    public Response<String> approveUserHours(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody HourRequestHandleRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.approveUserHours(token, userId,volunteeringId, body.getVolunteerId(), body.getStartDate(), body.getEndDate());
    }

    @PostMapping("/denyUserHours")
    public Response<String> denyUserHours(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody HourRequestHandleRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.denyUserHours(token, userId,volunteeringId, body.getVolunteerId(), body.getStartDate(), body.getEndDate());
    }

    @GetMapping("/getVolunteering")
    public Response<VolunteeringDTO> getVolunteering(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringDTO(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteeringSkills")
    public Response<List<String>> getVolunteeringSkills(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringSkills(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteeringCategories")
    public Response<List<String>> getVolunteeringCategories(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringCategories(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteeringLocations")
    public Response<List<LocationDTO>> getVolunteeringLocations(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringLocations(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteeringVolunteers")
    public Response<Map<String,Integer>> getVolunteeringVolunteers(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringVolunteers(token, userId, volunteeringId);
    }

    @GetMapping("/getGroup")
    public Response<GroupDTO> getGroup(@RequestParam String userId, @RequestParam int volunteeringId, @RequestParam int groupId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getGroupDTO(token, userId, volunteeringId,groupId);
    }

    @GetMapping("/getConstantCodes")
    public Response<List<String>> getConstantCodes(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getConstantCodes(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteerAppointments")
    public Response<List<ScheduleAppointmentDTO>> getVolunteerAppointments(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteerAppointments(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteerAvailableRanges")
    public Response<List<ScheduleRangeDTO>> getVolunteerAvailableRanges(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteerAvailableRanges(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteeringHourRequests")
    public Response<List<HourApprovalRequests>> getVolunteeringHourRequests(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringHourRequests(token, userId, volunteeringId);
    }
}
