package com.dogood.dogoodbackend.domain.volunteerings;

public class VolunteeringFacade {
    private VolunteeringRepository repository;
    //private OrganizationFacade organizationFacade


    public VolunteeringFacade(VolunteeringRepository repository) {
        this.repository = repository;
    }

    private boolean isManager(String userId, int organizationId){
        //return organizationFacade.isManager(userId, organizationId);
        return true;
    }

    public int createVolunteering(String userId, int organizationId, String name, String description){
        if(!isManager(userId, organizationId)){
            throw new IllegalArgumentException("User " + userId + " is not a manager in organization " + organizationId);
        }
        Volunteering newVol = repository.addVolunteering(organizationId, name, description);
        return newVol.getId();
    }
}
