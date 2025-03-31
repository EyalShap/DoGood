package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.volunteeringrequests.*;
import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointmentDTO;
import com.dogood.dogoodbackend.service.Response;
import com.dogood.dogoodbackend.service.VolunteeringService;
import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    @PatchMapping("/generateSkillsAndCategories")
    public Response<String> generateSkillsAndCategories(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.generateSkillsAndCategories(token, userId, volunteeringId);
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

    @GetMapping("/getVolunteeringScanType")
    public Response<ScanTypes> getVolunteeringScanType(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringScanType(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteeringApprovalType")
    public Response<ApprovalType> getVolunteeringApprovalType(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringApprovalType(token, userId, volunteeringId);
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
    public Response<String> removeGroup(@RequestParam String userId, @RequestParam int volunteeringId, @RequestParam int groupId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.removeGroup(token, userId, volunteeringId, groupId);
    }

    @DeleteMapping("/removeLocation")
    public Response<String> removeLocation(@RequestParam String userId,  @RequestParam int volunteeringId, @RequestParam int locId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.removeLocation(token, userId, volunteeringId, locId);
    }

    @DeleteMapping("/removeRange")
    public Response<String> removeRange(@RequestParam String userId, @RequestParam int volunteeringId, @RequestParam int rangeId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.removeRange(token, userId, volunteeringId, rangeId);
    }

    @PostMapping("/addScheduleRangeToGroup")
    public Response<Integer> addScheduleRangeToGroup(@RequestParam String userId, @RequestBody CreateRangeRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.addScheduleRangeToGroup(token, userId,
                body.getVolunteeringId(),
                body.getGroupId(),
                body.getLocId(),
                body.getStartHour(),
                body.getStartMinute(),
                body.getEndHour(),
                body.getEndMinute(),
                body.getMinimumMinutes(),
                body.getMaximumMinutes(),
                body.getWeekDays(),
                body.getOneTime());
    }

    @PostMapping("/addRestrictionToRange")
    public Response<String> addRestrictionToRange(@RequestParam String userId, @RequestBody Map<String, Integer> body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.addRestrictionToRange(token, userId,
                body.get("volunteeringId"),
                body.get("groupId"),
                body.get("locId"),
                body.get("rangeId"),
                body.get("startHour"),
                body.get("startMinute"),
                body.get("endHour"),
                body.get("endMinute"),
                body.get("amount")
        );
    }

    @DeleteMapping("/removeRestrictionFromRange")
    public Response<String> removeRestrictionFromRange(@RequestParam String userId,
                                                    @RequestParam int volunteeringId,
                                                    @RequestParam int groupId,
                                                    @RequestParam int locId,
                                                    @RequestParam int rangeId, @RequestParam int startHour, @RequestParam int startMinute, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.removeRestrictionFromRange(token, userId,volunteeringId, groupId, locId, rangeId, startHour, startMinute);
    }

    @PatchMapping("/updateRangeWeekdays")
    public Response<String> updateRangeWeekdays(@RequestParam String userId, @RequestBody UpdateRangeWeekdaysRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.updateRangeWeekdays(token, userId,
                body.getVolunteeringId(),
                body.getGroupId(),
                body.getWeekdays());
    }


    @PatchMapping("/updateRangeOneTimeDate")
    public Response<String> updateRangeOneTimeDate(@RequestParam String userId, @RequestBody UpdateRangeOneTimeRequest body, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.updateRangeOneTimeDate(token, userId,
                body.getVolunteeringId(),
                body.getGroupId(),
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

    @DeleteMapping("/cancelAppointment")
    public Response<String> cancelAppointment(@RequestParam String userId, @RequestParam int volunteeringId, @RequestParam int startHour, @RequestParam int startMinute, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.cancelAppointment(token, userId, volunteeringId, startHour, startMinute);
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

    @GetMapping("/getVolunteeringsOfUser")
    public Response<List<VolunteeringDTO>> getVolunteeringsOfUser(@RequestParam String userId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringsOfUser(token, userId);
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

    @GetMapping("/getGroupLocations")
    public Response<List<LocationDTO>> getGroupLocations(@RequestParam String userId, @RequestParam int volunteeringId, @RequestParam int groupId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getGroupLocations(token, userId, volunteeringId, groupId);
    }

    @GetMapping("/getVolunteeringGroups")
    public Response<List<Integer>> getVolunteeringGroups(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringGroups(token, userId, volunteeringId);
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

    @GetMapping("/getVolunteeringWarnings")
    public Response<List<String>> getVolunteeringWarnings(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringWarnings(token, userId, volunteeringId);
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

    @GetMapping("/getVolunteeringLocationGroupRanges")
    public Response<List<ScheduleRangeDTO>> getVolunteeringLocationGroupRanges(@RequestParam String userId, @RequestParam int volunteeringId, @RequestParam int groupId, @RequestParam int locId,HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringLocationGroupRanges(token, userId, volunteeringId, groupId, locId);
    }

    @GetMapping("/getVolunteeringHourRequests")
    public Response<List<HourApprovalRequest>> getVolunteeringHourRequests(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringHourRequests(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteeringJoinRequests")
    public Response<List<JoinRequest>> getVolunteeringJoinRequests(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteeringJoinRequests(token, userId, volunteeringId);
    }

    @GetMapping("/getUserAssignedLocation")
    public Response<Integer> getUserAssignedLocation(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getUserAssignedLocation(token, userId, volunteeringId);
    }

    @GetMapping("/getVolunteerGroup")
    public Response<Integer> getVolunteerGroup(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getVolunteerGroup(token, userId, volunteeringId);
    }

    @GetMapping("/getUserAssignedLocationData")
    public Response<LocationDTO> getUserAssignedLocationData(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.getUserAssignedLocationData(token, userId, volunteeringId);
    }

    @GetMapping("/userHasSettingsPermission")
    public Response<Boolean> userHasSettingsPermission(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.userHasSettingsPermission(token, userId, volunteeringId);
    }

    @DeleteMapping("/removeImageFromVolunteering")
    public Response<String> removeImageFromVolunteering(@RequestParam String userId, @RequestParam int volunteeringId, @RequestParam String imagePath, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.removeImage(token, userId, volunteeringId, imagePath);
    }

    @PostMapping("/addImageToVolunteering")
    public Response<String> addImageToVolunteering(@RequestParam String userId, @RequestParam int volunteeringId, @RequestBody String imagePath, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.addImage(token, userId, volunteeringId, imagePath.replaceAll("\"",""));
    }

    @GetMapping("/getUserApprovedHoursFormatted")
    public void getUserApprovedHoursFormatted(@RequestParam String userId, @RequestParam int volunteeringId, @RequestParam String israeliId, HttpServletRequest request, HttpServletResponse response) throws IOException, DocumentException {
        String token = getToken(request);
        Response<String> resp = volunteeringService.getUserApprovedHoursFormatted(token,userId,volunteeringId,israeliId);
        if(resp.getError()){
            response.setContentType("application/json");
            response.setStatus(400);
            response.getWriter().print(resp.getErrorString());
            response.getWriter().flush();
        }else{
            File file = new File(resp.getData());
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename="+file.getName());

            OutputStream outputStream = response.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream(file);

            IOUtils.copy(fileInputStream, outputStream);
            outputStream.close();
            fileInputStream.close();
            File parentDir =  file.getParentFile();
            file.delete();
            if(parentDir.isDirectory() && parentDir.list().length == 0) {
                parentDir.delete();
            }
        }
    }

    @DeleteMapping("/removeVolunteering")
    public Response<String> removeVolunteering(@RequestParam String userId, @RequestParam int volunteeringId, HttpServletRequest request){
        String token = getToken(request);
        return volunteeringService.removeVolunteering(token, userId, volunteeringId);
    }
}
