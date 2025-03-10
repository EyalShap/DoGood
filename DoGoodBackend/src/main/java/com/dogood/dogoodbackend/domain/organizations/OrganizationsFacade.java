package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
public class OrganizationsFacade {
    private OrganizationRepository organizationRepository;
    private RequestRepository requestRepository;
    private UsersFacade usersFacade;
    private VolunteeringFacade volunteeringFacade;
    private ReportsFacade reportsFacade;

    public OrganizationsFacade(UsersFacade usersFacade, OrganizationRepository organizationRepository, RequestRepository requestRepository) {
        this.usersFacade = usersFacade;
        this.organizationRepository = organizationRepository;
        this.requestRepository = requestRepository;
    }

    public void setVolunteeringFacade(VolunteeringFacade volunteeringFacade) {
        this.volunteeringFacade = volunteeringFacade;
    }

    public void setReportFacade(ReportsFacade reportsFacade) {
        this.reportsFacade = reportsFacade;
    }

    public int createOrganization(String name, String description, String phoneNumber, String email, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        int id =  organizationRepository.createOrganization(name, description, phoneNumber, email, actor);
        usersFacade.addUserOrganization(actor, id);
        return id;
    }

    public void removeOrganization(int organizationId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization toRemove = organizationRepository.getOrganization(organizationId);

        if(!toRemove.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, toRemove.getName(), "remove the organization"));
        }
        for(int volunteeringId : toRemove.getVolunteeringIds()) {
            volunteeringFacade.removeVolunteering(actor, volunteeringId);
        }
        requestRepository.removeObjectRequests(organizationId, RequestObject.ORGANIZATION);
        organizationRepository.setManagers(organizationId, new ArrayList<>());
        organizationRepository.setVolunteeringIds(organizationId, new ArrayList<>());
        organizationRepository.removeOrganization(organizationId);
        if(toRemove.isFounder(actor)) {
            usersFacade.removeUserOrganization(actor, organizationId);
        }
        reportsFacade.removeOrganizationReports(organizationId);
    }

    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization toEdit = organizationRepository.getOrganization(organizationId);

        if(!toEdit.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, toEdit.getName(), "edit the organization's details"));
        }
        organizationRepository.editOrganization(organizationId, name, description, phoneNumber, email);
    }

    public int createVolunteering(int organizationId, String volunteeringName, String volunteeringDescription, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganization(organizationId);
        if(!isManager(actor, organizationId) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "create a new volunteering"));
        }

        int volunteeringId = volunteeringFacade.createVolunteering(actor, organizationId, volunteeringName, volunteeringDescription);
        organization.addVolunteering(volunteeringId);
        organizationRepository.setVolunteeringIds(organizationId, organization.getVolunteeringIds());

        return volunteeringId;
    }

    public void removeVolunteering(int organizationId, int volunteeringId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganization(organizationId);

        if(!organization.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "remove a volunteering"));
        }
        organization.removeVolunteering(volunteeringId); // checks if volunteering exists
        organizationRepository.setVolunteeringIds(organizationId, organization.getVolunteeringIds());
    }

    public void sendAssignManagerRequest(String newManager, String actor, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganization(organizationId);
        if(!organization.isManager(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "send assign manager request"));
        }
        if(organization.isManager(newManager)) {
            throw new IllegalArgumentException(OrganizationErrors.makeUserIsAlreadyAManagerError(newManager, organization.getName()));
        }
        for(int volunteeringId : organization.getVolunteeringIds()) {
            if(volunteeringFacade.getHasVolunteer(newManager, volunteeringId)) {
                throw new IllegalArgumentException(OrganizationErrors.makeUserIsVolunteerInTheOrganizationError(newManager, organization.getName()));
            }
        }
        requestRepository.createRequest(newManager, actor, organizationId, RequestObject.ORGANIZATION);

        //TODO: change when users facade is implemented
        //usersFacade.notify(newManager, ....);
    }

    public void handleAssignManagerRequest(String actor, int organizationId, boolean approved) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Request request = requestRepository.getRequest(actor, organizationId, RequestObject.ORGANIZATION);
        Organization organization = organizationRepository.getOrganization(organizationId);
        if(approved) {
            organization.addManager(actor);
            organizationRepository.setManagers(organizationId, organization.getManagerUsernames());
        }
        requestRepository.deleteRequest(actor, organizationId, RequestObject.ORGANIZATION);

        String approvedStr = approved ? "approved" : "denied";
        String message = String.format("%s has %s your assign as manager request.", actor, approvedStr);
        //TODO: change when users facade is implemented
        //usersFacade.notify(request.getAssignerUsername(), message);
    }

    public void resign(String actor, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganization(organizationId);
        organization.resign(actor);
        organizationRepository.setManagers(organizationId, organization.getManagerUsernames());
    }

    public void removeManager(String actor, String managerToRemove, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganization(organizationId);

        if(!organization.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, organization.getName(), "remove a manager from organization"));
        }

        organization.removeManager(managerToRemove);
        organizationRepository.setManagers(organizationId, organization.getManagerUsernames());
    }

    public void setFounder(String actor, String newFounder, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganization(organizationId);

        if(!organization.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, organization.getName(), "set a new founder to the organization"));
        }

        organizationRepository.setFounder(organizationId, newFounder);
    }

    public List<Request> getUserRequests(String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        return requestRepository.getUserRequests(actor, RequestObject.ORGANIZATION);
    }

    public OrganizationDTO getOrganization(int organizationId) {
        Organization organization = organizationRepository.getOrganization(organizationId);
        OrganizationDTO organizationDTO = new OrganizationDTO(organization);
        return organizationDTO;
    }

    public List<Integer> getUserVolunteerings(int organizationId, String actor) {
        List<Integer> res = new ArrayList<>();
        List<Integer> orgVolunteeringIds = getOrganization(organizationId).getVolunteeringIds();
        for(int volunteeringId : orgVolunteeringIds) {
            if(volunteeringFacade.getHasVolunteer(actor, volunteeringId)) {
                res.add(volunteeringId);
            }
        }
        return res;
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

    private boolean isAdmin(String user) {
        return usersFacade.isAdmin(user);
    }
    private boolean userExists(String user){
        return usersFacade.userExists(user);
    }
}
