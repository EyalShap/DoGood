package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationNavigations;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class OrganizationsFacade {
    private OrganizationRepository organizationRepository;
    private RequestRepository requestRepository;
    private UsersFacade usersFacade;
    private VolunteeringFacade volunteeringFacade;
    private ReportsFacade reportsFacade;
    private NotificationSystem notificationSystem;

    public OrganizationsFacade(UsersFacade usersFacade, OrganizationRepository organizationRepository, RequestRepository requestRepository) {
        this.usersFacade = usersFacade;
        this.organizationRepository = organizationRepository;
        this.requestRepository = requestRepository;
    }

    public void setVolunteeringFacade(VolunteeringFacade volunteeringFacade) {
        this.volunteeringFacade = volunteeringFacade;
    }

    public void setNotificationSystem(NotificationSystem notificationSystem) {
        this.notificationSystem = notificationSystem;
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
        Organization toRemove = organizationRepository.getOrganizationForWrite(organizationId);

        if(!toRemove.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, toRemove.getName(), "remove the organization"));
        }

        List<Integer> volunteeringIds = new ArrayList<>(toRemove.getVolunteeringIds());
        for(int volunteeringId : volunteeringIds) {
            volunteeringFacade.removeVolunteering(actor, volunteeringId);
        }
        requestRepository.removeObjectRequests(organizationId, RequestObject.ORGANIZATION);
        organizationRepository.setVolunteeringIds(organizationId, new ArrayList<>());
        if(toRemove.isManager(actor)) {
            usersFacade.removeUserOrganization(actor, organizationId);
        }
        reportsFacade.removeOrganizationReports(organizationId);

        notifyManagers(String.format("Your organization \"%s\" was removed.", toRemove.getName()), NotificationNavigations.organizationList, organizationId);
        organizationRepository.setManagers(organizationId, new ArrayList<>());
        organizationRepository.removeOrganization(organizationId);
    }

    public void editOrganization(int organizationId, String name, String description, String phoneNumber, String email, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization toEdit = organizationRepository.getOrganizationForWrite(organizationId);
        String prevName = toEdit.getName();

        if(!toEdit.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, toEdit.getName(), "edit the organization's details"));
        }
        organizationRepository.editOrganization(organizationId, name, description, phoneNumber, email);

        notifyManagers(String.format("Your organization \"%s\" was edited.", prevName), NotificationNavigations.organization(organizationId), organizationId);
    }

    public int createVolunteering(int organizationId, String volunteeringName, String volunteeringDescription, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganizationForWrite(organizationId);
        if(!isManager(actor, organizationId) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "create a new volunteering"));
        }

        int volunteeringId = volunteeringFacade.createVolunteering(actor, organizationId, volunteeringName, volunteeringDescription);
        organization.addVolunteering(volunteeringId);
        organizationRepository.setVolunteeringIds(organizationId, organization.getVolunteeringIds());

        notifyManagers(String.format("A new volunteering \"%s\" was added to your organization \"%s\".", volunteeringName, organization.getName()), NotificationNavigations.volunteering(volunteeringId), organizationId);
        return volunteeringId;
    }

    public void removeVolunteering(int organizationId, int volunteeringId, String actor, String volunteeringName) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganizationForWrite(organizationId);

        if(!organization.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "remove a volunteering"));
        }
        organization.removeVolunteering(volunteeringId); // checks if volunteering exists
        organizationRepository.setVolunteeringIds(organizationId, organization.getVolunteeringIds());

        notifyManagers(String.format("The volunteering \"%s\" was removed from your organization \"%s\".", volunteeringName, organization.getName()), NotificationNavigations.organization(organizationId), organizationId);
    }

    public void removeVolunteering(int organizationId, int volunteeringId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganizationForWrite(organizationId);
        String volunteeringName = volunteeringFacade.getVolunteeringDTO(volunteeringId).getName();

        if(!organization.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "remove a volunteering"));
        }
        organization.removeVolunteering(volunteeringId); // checks if volunteering exists
        organizationRepository.setVolunteeringIds(organizationId, organization.getVolunteeringIds());

        notifyManagers(String.format("The volunteering \"%s\" was removed from your organization \"%s\".", volunteeringName, organization.getName()), NotificationNavigations.organization(organizationId), organizationId);
    }

    public void sendAssignManagerRequest(String newManager, String actor, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        if(!userExists(newManager)){
            throw new IllegalArgumentException("User " + newManager + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganizationForRead(organizationId);
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

        String message = String.format("%s is asking you to be manager of organization \"%s\".", actor, organization.getName());
        notificationSystem.notifyUser(newManager, message, NotificationNavigations.requests);
    }

    public void handleAssignManagerRequest(String actor, int organizationId, boolean approved) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Request request = requestRepository.getRequest(actor, organizationId, RequestObject.ORGANIZATION);
        Organization organization = organizationRepository.getOrganizationForWrite(organizationId);
        if(approved) {
            organization.addManager(actor);
            organizationRepository.setManagers(organizationId, organization.getManagerUsernames());
        }
        requestRepository.deleteRequest(actor, organizationId, RequestObject.ORGANIZATION);

        String approvedStr = approved ? "approved" : "denied";
        String message = String.format("%s has %s your assign as manager request.", actor, approvedStr);
        notificationSystem.notifyUser(request.getAssignerUsername(), message, NotificationNavigations.organization(organizationId));
    }

    public void resign(String actor, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganizationForWrite(organizationId);
        organization.resign(actor);
        organizationRepository.setManagers(organizationId, organization.getManagerUsernames());

        notifyManagers(String.format("The manager \"%s\" resigned from your organization \"%s\".", actor, organization.getName()), NotificationNavigations.organization(organizationId), organizationId);
    }

    public void removeManager(String actor, String managerToRemove, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        if(!userExists(managerToRemove)){
            throw new IllegalArgumentException("User " + managerToRemove + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganizationForWrite(organizationId);

        if(!organization.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, organization.getName(), "remove a manager from organization"));
        }

        organization.removeManager(managerToRemove);
        organizationRepository.setManagers(organizationId, organization.getManagerUsernames());
        notificationSystem.notifyUser(managerToRemove, String.format("You are no longer a manager of organization \"%s\".", organization.getName()), NotificationNavigations.organization(organizationId));
    }

    public void setFounder(String actor, String newFounder, int organizationId) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        if(!userExists(newFounder)){
            throw new IllegalArgumentException("User " + newFounder + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganizationForRead(organizationId);

        if(!organization.isFounder(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonFounderCanNotPreformActionError(actor, organization.getName(), "set a new founder to the organization"));
        }

        organizationRepository.setFounder(organizationId, newFounder); // will lock org for read

        notifyManagers(String.format("%s is the new founder of your organization \"%s\".", newFounder, organization.getName()), NotificationNavigations.organization(organizationId), organizationId);
    }

    public void notifyManagers(String message, String nav, int orgId){
        //Hi dana this is function eyal added
        //You can change it if you want
        Organization organization = organizationRepository.getOrganizationForRead(orgId);
        for(String username : organization.getManagerUsernames()){
            notificationSystem.notifyUser(username, message, nav);
        }
    }

    public List<Request> getUserRequests(String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        return requestRepository.getUserRequests(actor, RequestObject.ORGANIZATION);
    }

    public OrganizationDTO getOrganization(int organizationId) {
        Organization organization = organizationRepository.getOrganizationForRead(organizationId);
        OrganizationDTO organizationDTO = new OrganizationDTO(organization);
        return organizationDTO;
    }

    public List<Integer> getUserVolunteerings(int organizationId, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        List<Integer> res = new ArrayList<>();
        List<Integer> orgVolunteeringIds = organizationRepository.getOrganizationForRead(organizationId).getVolunteeringIds();
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
        if(!userExists(username)){
            throw new IllegalArgumentException("User " + username + " doesn't exist");
        }
        return organizationRepository.getOrganizationForRead(organizationId).isManager(username);
    }

    public List<VolunteeringDTO> getOrganizationVolunteerings(int organizationId) {
        Organization organization = organizationRepository.getOrganizationForRead(organizationId);
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

    public void changeImageInOrganization(int organizationId, String image, boolean add, String actor) {
        if(!userExists(actor)){
            throw new IllegalArgumentException("User " + actor + " doesn't exist");
        }
        Organization organization = organizationRepository.getOrganizationForRead(organizationId);
        if(!organization.isManager(actor) && !isAdmin(actor)) {
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(actor, organization.getName(), "add image"));
        }

        if(image.charAt(0) == '\"') {
            int len = image.length();
            image = image.substring(1, len - 1);
        }

        List<String> newImages = new ArrayList<>(organization.getImagePaths());
        if(add) {
            newImages.add(image);
        }
        else {
            newImages.remove(image);
        }
        organizationRepository.setImages(organizationId, newImages); // will lock org for write
    }

    public void uploadSignature(int organizationId, String actor, MultipartFile signature) {
        organizationRepository.uploadSignature(organizationId, actor, signature);
    }

    public byte[] getSignature(int organizationId, String actor) {
        return organizationRepository.getSignature(organizationId, actor);
    }
}
