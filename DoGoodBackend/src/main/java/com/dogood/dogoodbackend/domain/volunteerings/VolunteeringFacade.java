package com.dogood.dogoodbackend.domain.volunteerings;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.DatePair;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointment;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingFacade;

import java.time.ZoneId;
import java.util.Date;

public class VolunteeringFacade {
    private VolunteeringRepository repository;
    private final int MINUTES_ALLOWED = 15;
    private SchedulingFacade schedulingFacade;
    //private OrganizationFacade organizationFacade;
    //private UserFacade userFacade;


    public VolunteeringFacade(VolunteeringRepository repository, SchedulingFacade schedulingFacade) {
        this.schedulingFacade = schedulingFacade;
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
        Date first = repository.getFirstVolunteerScan(volunteeringId, userId);
        DatePair p = null;
        if(first == null){
            if(volunteering.getScanTypes() == ScanTypes.ONE_SCAN){
                p = schedulingFacade.convertSingleTimeToAppointmentRange(userId, volunteeringId, new Date());
            }else{
                repository.recordFirstVolunteerScan(volunteeringId, userId);
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
}
