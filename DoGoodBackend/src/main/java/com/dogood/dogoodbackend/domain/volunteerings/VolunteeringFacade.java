package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.*;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Transactional
public class VolunteeringFacade {
    private VolunteeringRepository repository;
    private final int MINUTES_ALLOWED = 15;
    private final double APPROVAL_PERCENTAGE = 0.75;
    private SchedulingFacade schedulingFacade;
    private OrganizationsFacade organizationFacade;
    private PostsFacade postsFacade;
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

    public void setPostsFacade(PostsFacade postsFacade) {
        this.postsFacade = postsFacade;
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

    private boolean isAdmin(String userId){
        //return userFacade.isAdmin(userId);
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
        if(!isAdmin(userId) || !isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " cannot remove volunteering " + volunteeringId);
        }
        repository.disableVolunteering(volunteeringId);
        postsFacade.removePostsByVolunteeringId(volunteeringId);
        schedulingFacade.removeAppointmentsAndRequestsForVolunteering(volunteeringId);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        boolean approvalOk = true;
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
            long timeDid = TimeUnit.MILLISECONDS.toMinutes(second.getTime() - first.getTime());
            long timeRequired = TimeUnit.MILLISECONDS.toMinutes(p.getEnd().getTime() - p.getStart().getTime());
            approvalOk = timeDid >= timeRequired*APPROVAL_PERCENTAGE;
        }
        schedulingFacade.addHourApprovalRequest(userId, volunteeringId, p.getStart(), p.getEnd());
        if(volunteering.getApprovalType() == ApprovalType.AUTO_FROM_SCAN && approvalOk){
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
        repository.updateVolunteeringInDB(volunteering);
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
        if(isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is already a manager in organization of volunteering " + volunteeringId);
        }
        volunteering.addJoinRequest(userId, new JoinRequest(userId, freeText));
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        repository.updateVolunteeringInDB(volunteering);
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
        schedulingFacade.approveUserHours(volunteerId, volunteeringId, start, end);
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
        schedulingFacade.denyUserHours(volunteerId, volunteeringId, start, end);
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


    public List<ApprovedHours> getUserApprovedHours(String userId, List<Integer> volunteeringIds){
        return schedulingFacade.getUserApprovedHours(userId,volunteeringIds);
    }


    public List<ScheduleAppointmentDTO> getVolunteerAppointments(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        return schedulingFacade.getUserAppointments(userId, volunteeringId);
    }


    public List<ScheduleRangeDTO> getVolunteerAvailableRanges(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        return volunteering.getVolunteerAvailableRanges(userId);
    }


    public List<HourApprovalRequests> getVolunteeringHourRequests(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        return schedulingFacade.getHourApprovalRequests(volunteeringId);
    }


    public Map<String, JoinRequest> getVolunteeringJoinRequests(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + volunteering.getOrganizationId() + " of volunteering " + volunteeringId);
        }
        return volunteering.getPendingJoinRequests();
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


    public boolean getHasVolunteer(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.hasVolunteer(userId);
    }


    public int getUserAssignedLocation(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getAssignedLocation(userId);
    }

    public int getVolunteerGroup(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getVolunteerGroup(userId);
    }

    public LocationDTO getUserAssignedLocationData(String userId, int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getAssignedLocationData(userId);
    }


    public List<PastExperience> getVolunteeringPastExperiences(int volunteeringId){
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        return volunteering.getPastExperiences();
    }
}
