package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.utils.OrganizationErrors;

import java.util.ArrayList;
import java.util.List;

public class OrganizationsFacade {
    private OrganizationRepository organizationRepository;
    private RequestRepository requestRepository;
    //private UsersFacade usersFacade;
    private VolunteeringFacade volunteeringFacade;

    public OrganizationsFacade(OrganizationRepository organizationRepository, RequestRepository requestRepository) {
        this.organizationRepository = organizationRepository;
        this.requestRepository = requestRepository;
    }

    public void setVolunteeringFacade(VolunteeringFacade volunteeringFacade) {
        this.volunteeringFacade = volunteeringFacade;
    }

    public int createOrganization(String name, String description, String phoneNumber, String email, String actor) {
        //TODO: check if user exists and logged in

        return organizationRepository.createOrganization(name, description, phoneNumber, email, actor);
    }

    public void removeOrganization(int organizationId, String actor) {
        //TODO: check if user exists and logged in

        Organization toRemove = organizationRepository.getOrganization(organizationId);

        if(!toRemove.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, toRemove.getName(), "remove the organization"));
        }
        organizationRepository.removeOrganization(organizationId);
    }

    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email, String actor) {
        //TODO: check if user exists and logged in

        Organization toEdit = organizationRepository.getOrganization(organizationId);

        if(!toEdit.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, toEdit.getName(), "edit the organization's details"));
        }
        organizationRepository.editOrganization(organizationId, name, description, phoneNumber, email);
    }

    public int createVolunteering(int organizationId, String volunteeringName, String volunteeringDescription, String actor) {
        //TODO: check if user exists and logged in

        Organization organization = organizationRepository.getOrganization(organizationId);
        if(!organization.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "create a new volunteering"));
        }

        int volunteeringId = volunteeringFacade.createVolunteering(actor, organizationId, volunteeringName, volunteeringDescription);
        organization.addVolunteering(volunteeringId);

        return volunteeringId;
    }

    public void removeVolunteering(int organizationId, int volunteeringId, String actor) {
        //TODO: check if user exists and logged in

        Organization organization = organizationRepository.getOrganization(organizationId);

        if(!organization.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "remove a volunteering"));
        }
        organization.removeVolunteering(volunteeringId); // checks if volunteering exists
    }

    public void sendAssignManagerRequest(String newManager, String actor, int organizationId) {
        //TODO: check if user exists and logged in

        Organization organization = organizationRepository.getOrganization(organizationId);
        if(!organization.isManager(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "send assign manager request"));
        }
        if(organization.isManager(newManager)) {
            throw new IllegalArgumentException(OrganizationErrors.makeUserIsAlreadyAManagerError(newManager, organization.getName()));
        }
        requestRepository.createRequest(newManager, actor, organizationId);

        //TODO: change when users facade is implemented
        //usersFacade.notify(newManager, ....);
    }

    public void handleAssignManagerRequest(String actor, int organizationId, boolean approved) {
        //TODO: check if user exists and logged in

        Request request = requestRepository.getRequest(actor, organizationId);
        Organization organization = organizationRepository.getOrganization(organizationId);

        if(approved) {
            organization.addManager(actor);
        }
        requestRepository.deleteRequest(actor, organizationId);

        String approvedStr = approved ? "approved" : "denied";
        String message = String.format("%s has %s your assign as manager request.", actor, approvedStr);
        //TODO: change when users facade is implemented
        //usersFacade.notify(request.getAssignerUsername(), message);
    }

    public void resign(String actor, int organizationId) {
        //TODO: check if user exists and logged in

        Organization organization = organizationRepository.getOrganization(organizationId);
        organization.resign(actor);
    }

    public void removeManager(String actor, String managerToRemove, int organizationId) {
        //TODO: check if user exists and logged in

        Organization organization = organizationRepository.getOrganization(organizationId);

        if(!organization.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, organization.getName(), "remove a manager from organization"));
        }

        organization.removeManager(managerToRemove);
    }

    public void setFounder(String actor, String newFounder, int organizationId) {
        //TODO: check if user exists and logged in

        Organization organization = organizationRepository.getOrganization(organizationId);

        if(!organization.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, organization.getName(), "set a new founder to the organization"));
        }

        organization.setFounder(newFounder);
    }

    public List<Request> getUserRequests(String actor) {
        //TODO: check if user exists and logged in

        return requestRepository.getUserRequests(actor);
    }

    public OrganizationDTO getOrganization(int organizationId) {
        Organization organization = organizationRepository.getOrganization(organizationId);
        OrganizationDTO organizationDTO = new OrganizationDTO(organization);
        return organizationDTO;
    }

    public List<OrganizationDTO> getAllOrganizations() {
        return organizationRepository.getAllOrganizationDTOs();
    }

    public boolean isManager(String username, int organizationId) {
        return organizationRepository.getOrganization(organizationId).isManager(username);
    }

    public List<VolunteeringDTO> getOrganizationVolunteerings(int organizationId) {
        OrganizationDTO organization = getOrganization(organizationId);
        List<Integer> volunteeringIds = organization.getVolunteeringIds();
        List<VolunteeringDTO> volunteeringDTOS = new ArrayList<>();

        for(int volunteeringId : volunteeringIds) {
            volunteeringDTOS.add(volunteeringFacade.getVolunteeringDTO(volunteeringId));
        }
        return volunteeringDTOS;
    }

    // TODO: remove when users facade is implemented
    private boolean isAdmin(String username) {
        return false;
    }


}
