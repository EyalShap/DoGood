package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.utils.OrganizationErrors;

public class PostsFacade {
    private VolunteeringPostRepository volunteeringPostRepository;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;

    public PostsFacade(VolunteeringPostRepository volunteeringPostRepository, VolunteeringFacade volunteeringFacade, OrganizationsFacade organizationsFacade) {
        this.volunteeringPostRepository = volunteeringPostRepository;
        this.volunteeringFacade = volunteeringFacade;
        this.organizationsFacade = organizationsFacade;
    }

    /*public int createVolunteeringPost(String title, String description, String posterUsername, int volunteeringId) {
        //TODO: check if user exists and logged in

        VolunteeringDTO volunteeringDTO = volunteeringFacade.getVolunteeringDTO(volunteeringId);
        int organizationId = volunteeringDTO.getId();

        // only managers in the organization can post about the organization's volunteering
        if(!organizationsFacade.isManager(posterUsername, organizationId)) {
            String organizationName = organizationsFacade.getOrganization(organizationId).getName();
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(posterUsername, organizationName, "post about the organization's volunteering"));
        }

        return volunteeringPostRepository.createVolunteeringPost(title, description, posterUsername, volunteeringId, organizationId);
    }

    public void removeVolunteeringPost(int postId, String actor) {
        //TODO: check if user exists and logged in

        Post toRemove = volunteeringPostRepository.getVolunteeringPost(postId);

        if(!toRemove.getPosterUsername().equals(actor) && !isAdmin(actor)) {
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
    }*/

    public boolean doesExist(int postId) {
        return true;
    }

    // TODO: remove when users facade is implemented
    private boolean isAdmin(String username) {
        return false;
    }

}
