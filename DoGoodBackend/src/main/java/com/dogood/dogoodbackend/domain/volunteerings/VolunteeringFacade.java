package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class VolunteeringFacade {
    private VolunteeringRepository repository;
    private final int MINUTES_ALLOWED = 15;
    private SchedulingFacade schedulingFacade;
    private OrganizationsFacade organizationFacade;
    //private UserFacade userFacade;


    public VolunteeringFacade(OrganizationsFacade organizationsFacade, VolunteeringRepository repository, SchedulingManager schedulingManager) {
        this.schedulingFacade = new SchedulingFacade(schedulingManager);
        this.organizationFacade = organizationsFacade;
        this.repository = repository;
    }

    private boolean isManager(String userId, int organizationId){
        loggedInCheck(userId);
        return organizationFacade.isManager(userId, organizationId);
    }

    private void loggedInCheck(String userId){
        //do a logged in check and throw exception if not because i forgot
        if(false){
            throw new IllegalArgumentException("User " + userId + " is not logged in");
        }
    }

    private boolean userExists(String userId){
        loggedInCheck(userId);
        //return userFacade.userExists(userId);
        return true;
    }

    public int createVolunteering(String userId, int organizationId, String name, String description){
        if(!isManager(userId, organizationId)){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + organizationId);
        }
        Volunteering newVol = repository.addVolunteering(organizationId, name, description);
        return newVol.getId();
    }

    public void removeVolunteering(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        repository.disableVolunteering(volunteeringId);
        organizationFacade.removeVolunteering(volunteering.getOrganizationId(), volunteeringId, userId);
    }

    public void updateVolunteering(String userId, int volunteeringId, String name, String description){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        repository.updateVolunteering(volunteeringId, name, description);
    }

    public void updateVolunteeringSkills(String userId, int volunteeringId, List<String> skills){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        repository.updateVolunteeringSkills(volunteeringId, skills);
    }

    public void updateVolunteeringCategories(String userId, int volunteeringId, List<String> categories){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        repository.updateVolunteeringCategories(volunteeringId, categories);
    }

    public void updateVolunteeringScanDetails(String userId, int volunteeringId, ScanTypes scanTypes, ApprovalType approvalType){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.setScanTypes(scanTypes);
        volunteering.setApprovalType(approvalType);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void addImageToVolunteering(String userId, int volunteeringId, String imagePath){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.addImagePath(imagePath);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void removeImageFromVolunteering(String userId, int volunteeringId, String imagePath){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.removeImagePath(imagePath);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void scanCode(String userId, String code){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        String[] parts = code.replaceAll("\"", "").split(":");
        int volunteeringId = -1;
        try{
            volunteeringId = Integer.parseInt(parts[0]);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid volunteering id");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        if(volunteering.getScanTypes() == ScanTypes.NO_SCAN){
            throw new UnsupportedOperationException("Volunteering " + volunteeringId + " does not support QR codes");
        }
        if(!volunteering.codeValid(parts[1])){
            throw new IllegalArgumentException("Invalid code");
        }
        Date first = repository.getFirstVolunteerScan(volunteeringId, userId);
        DatePair p = null;
        if(first == null){
            if(volunteering.getScanTypes() == ScanTypes.ONE_SCAN){
                p = schedulingFacade.convertSingleTimeToAppointmentRange(userId, volunteeringId, new Date(), MINUTES_ALLOWED);
            }else{
                repository.recordFirstVolunteerScan(volunteeringId, userId);
                return;
            }
        }else{
            if(volunteering.getScanTypes() != ScanTypes.DOUBLE_SCAN){
                throw new UnsupportedOperationException("Volunteering " + volunteeringId + " does not support double scans");
            }
            Date second = new Date();
            p = schedulingFacade.convertRoughRangeToAppointmentRange(userId, volunteeringId, first, second, MINUTES_ALLOWED);
            repository.removeFirstVolunteerScan(volunteeringId, userId);
        }
        schedulingFacade.addHourApprovalRequest(userId, volunteeringId, p.getStart(), p.getEnd());
        if(volunteering.getApprovalType() == ApprovalType.AUTO_FROM_SCAN){
            schedulingFacade.approveUserHours(userId, volunteeringId, p.getStart(), p.getEnd()); //yipee
        }
    }

    public String makeVolunteeringCode(String userId, int volunteeringId, boolean constant){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        String code = volunteering.generateCode(constant);
        repository.updateVolunteeringInDB(volunteeringId);
        return code;
    }

    public void requestToJoinVolunteering(String userId, int volunteeringId, String freeText){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is already a volunteer in volunteering " + volunteeringId);
        }
        volunteering.addJoinRequest(userId, new JoinRequest(userId, freeText));
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void acceptUserJoinRequest(String userId, int volunteeringId, String joinerId, int groupId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        if(!userExists(joinerId)){
            throw new IllegalArgumentException("User " + joinerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        if(volunteering.hasVolunteer(joinerId)){
            throw new IllegalArgumentException("User " + joinerId + " is already a volunteer in volunteering " + volunteeringId);
        }
        volunteering.approveJoinRequest(joinerId, groupId);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void denyUserJoinRequest(String userId, int volunteeringId, String joinerId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        if(!userExists(joinerId)){
            throw new IllegalArgumentException("User " + joinerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        if(volunteering.hasVolunteer(joinerId)){
            throw new IllegalArgumentException("User " + joinerId + " is already a volunteer in volunteering " + volunteeringId);
        }
        volunteering.denyJoinRequest(joinerId);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void finishVolunteering(String userId, int volunteeringId, String experience){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.leaveVolunteering(userId, new PastExperience(userId, experience, new Date()));
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public int addVolunteeringLocation(String userId, int volunteeringId, String name, AddressTuple address){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        int locId = volunteering.addLocation(name, address);
        repository.updateVolunteeringInDB(volunteeringId);
        return locId;
    }

    public void assignVolunteerToLocation(String userId, String volunteerId, int volunteeringId, int locId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        if(!userExists(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        if(!userId.equals(volunteerId) && !isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " cannot assign " + volunteerId + " to a location");
        }
        volunteering.assignVolunteerToLocation(volunteerId, locId);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void moveVolunteerGroup(String userId, String volunteerId, int volunteeringId, int groupId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        if(!userExists(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.moveVolunteerToNewGroup(volunteerId, groupId);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public int createNewGroup(String userId, int volunteeringId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        int groupId = volunteering.addNewGroup();
        repository.updateVolunteeringInDB(volunteeringId);
        return groupId;
    }

    public void removeGroup(String userId, int volunteeringId, int groupId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.removeGroup(groupId);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void removeLocation(String userId, int volunteeringId, int locId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.removeLocation(locId);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public int addScheduleRangeToGroup(String userId, int volunteeringId, int groupId, int locId, LocalTime startTime, LocalTime endTime, int minimumMinutes, int maximumMinutes){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        int rangeId = volunteering.addRangeToGroup(groupId, locId, startTime, endTime, minimumMinutes, maximumMinutes);
        repository.updateVolunteeringInDB(volunteeringId);
        return rangeId;
    }

    public void updateRangeWeekdays(String userId, int volunteeringId, int groupId, int locId, int rangeId, boolean[] weekdays){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.updateRangeWeekdays(groupId, locId, rangeId, weekdays);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void updateRangeOneTimeDate(String userId, int volunteeringId, int groupId, int locId, int rangeId, LocalDate oneTime){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.updateRangeOneTimeDate(groupId, locId, rangeId, oneTime);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void makeAppointment(String userId, int volunteeringId, int groupId, int locId, int rangeId, LocalTime start, LocalTime end, boolean[] weekdays, LocalDate oneTime){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        ScheduleRange range = volunteering.getScheduleRange(groupId, locId, rangeId);
        schedulingFacade.makeAppointment(userId, volunteeringId, range, start, end, weekdays, oneTime);
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void clearConstantCodes(String userId, int volunteeringId){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }

        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId());
        }
        volunteering.clearConstantCodes();
        repository.updateVolunteeringInDB(volunteeringId);
    }

    public void requestHoursApproval(String userId, int volunteeringId, Date start, Date end){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        schedulingFacade.addHourApprovalRequest(userId, volunteeringId, start, end);
    }

    public void approveUserHours(String userId, int volunteeringId, String volunteerId, Date start, Date end){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        if(!userExists(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        schedulingFacade.approveUserHours(userId, volunteeringId, start, end);
    }

    public void denyUserHours(String userId, int volunteeringId, String volunteerId, Date start, Date end){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        if(!userExists(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        schedulingFacade.denyUserHours(userId, volunteeringId, start, end);
    }

    public VolunteeringDTO getVolunteeringDTO(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getDTO();
    }

    public List<String> getVolunteeringSkills(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getSkills();
    }

    public List<String> getVolunteeringCategories(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getCategories();
    }

    public int getVolunteeringOrganizationId(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getOrganizationId();
    }

    public List<LocationDTO> getVolunteeringLocations(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getLocationDTOs();
    }

    public Map<String,Integer> getVolunteeringVolunteers(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getVolunteerToGroup();
    }

    public GroupDTO getGroupDTO(int volunteeringId, int groupId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getGroupDTO(groupId);
    }

    public List<String> getConstantCodes(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        return volunteering.getConstantCodes();
    }

    public void checkViewingPermissions(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId) && !isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " has no permission to view " + " volunteering with id " + volunteeringId);
        }
    }
}
