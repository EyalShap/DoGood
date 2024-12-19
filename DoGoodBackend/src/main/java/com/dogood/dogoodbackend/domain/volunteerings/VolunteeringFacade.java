package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public class VolunteeringFacade {
    private VolunteeringRepository repository;
    private final int MINUTES_ALLOWED = 15;
    private SchedulingFacade schedulingFacade;
    //private OrganizationFacade organizationFacade;
    //private UserFacade userFacade;


    public VolunteeringFacade(VolunteeringRepository repository, SchedulingManager schedulingManager) {
        this.schedulingFacade = new SchedulingFacade(schedulingManager);
        this.repository = repository;
    }

    private boolean isManager(String userId, int organizationId){
        //return organizationFacade.isManager(userId, organizationId);
        return true;
    }

    private boolean userExists(String userId){
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

    public void scanCode(String userId, String code){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        String[] parts = code.split(":");
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
        if(volunteering.getScanTypes() != ScanTypes.NO_SCAN){
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
        return volunteering.generateCode(constant);
    }

    public void requestToJoinVolunteering(String userId, int volunteeringId, String freeText){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!volunteering.hasVolunteer(userId)){
            throw new IllegalArgumentException("User " + userId + " is already a volunteer in volunteering " + volunteeringId);
        }
        volunteering.addJoinRequest(userId, new JoinRequest(userId, freeText));
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
            throw new IllegalArgumentException("User " + userId + " is already a volunteer in volunteering " + volunteeringId);
        }
        volunteering.approveJoinRequest(joinerId, groupId);
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
    }

    public int addVolunteeringLocation(String userId, int volunteeringId, String name, String address){
        if(!userExists(userId)){
            throw new IllegalArgumentException("User " + userId + " does not exist");
        }
        Volunteering volunteering = repository.getVolunteering(volunteeringId);
        if(volunteering == null){
            throw new IllegalArgumentException("Volunteering with id " + volunteeringId + " does not exist");
        }
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        return volunteering.addLocation(name, address);
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
        if(!isManager(userId, volunteering.getOrganizationId())){
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.assignVolunteerToLocation(volunteerId, locId);
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
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        if(!volunteering.hasVolunteer(volunteerId)){
            throw new IllegalArgumentException("User " + volunteerId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.moveVolunteerToNewGroup(volunteerId, groupId);
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
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        return volunteering.addNewGroup();
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
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.removeGroup(groupId);
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
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.removeLocation(locId);
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
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        return volunteering.addRangeToGroup(groupId, locId, startTime, endTime, minimumMinutes, maximumMinutes);
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
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.updateRangeWeekdays(groupId, locId, rangeId, weekdays);
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
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.updateRangeOneTimeDate(groupId, locId, rangeId, oneTime);
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
        List<RestrictionTuple> restrictionTuples = range.checkCollision(start, end);
        schedulingFacade.makeAppointment(userId, volunteeringId, rangeId, start, end, weekdays, oneTime, restrictionTuples);
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
            throw new IllegalArgumentException("User " + userId + " is not a volunteer in volunteering " + volunteeringId);
        }
        volunteering.clearConstantCodes();
    }
}
