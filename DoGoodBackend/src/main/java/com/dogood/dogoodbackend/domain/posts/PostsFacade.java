package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringDTO;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.PostErrors;

import java.util.List;

public class PostsFacade {
    private VolunteeringPostRepository volunteeringPostRepository;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;

    public PostsFacade(VolunteeringPostRepository volunteeringPostRepository, VolunteeringFacade volunteeringFacade, OrganizationsFacade organizationsFacade) {
        this.volunteeringPostRepository = volunteeringPostRepository;
        this.volunteeringFacade = volunteeringFacade;
        this.organizationsFacade = organizationsFacade;
    }

    public int createVolunteeringPost(String title, String description, String posterUsername, int volunteeringId) {
        //TODO: check if user exists and logged in

        int organizationId = volunteeringFacade.getVolunteeringOrganizationId(volunteeringId);

        // only managers in the organization can post about the organization's volunteering
        if(!organizationsFacade.isManager(posterUsername, organizationId) && !isAdmin(posterUsername)) {
            String organizationName = organizationsFacade.getOrganization(organizationId).getName();
            throw new IllegalArgumentException(OrganizationErrors.makeNonManagerCanNotPreformActionError(posterUsername, organizationName, "post about the organization's volunteering"));
        }

        return volunteeringPostRepository.createVolunteeringPost(title, description, posterUsername, volunteeringId, organizationId);
    }

    private boolean isAllowedToMakePostAction(String actor, VolunteeringPost post) {
        if(isAdmin(actor)) {
            return true;
        }

        int organizationId = volunteeringFacade.getVolunteeringOrganizationId(post.getVolunteeringId());
        if(organizationsFacade.isManager(actor, organizationId)) {
            return true;
        }

        // create post
        if(post == null) {
            return true;
        }

        return post.getPosterUsername().equals(actor);
    }

    public void removeVolunteeringPost(int postId, String actor) {
        //TODO: check if user exists and logged in

        VolunteeringPost toRemove = volunteeringPostRepository.getVolunteeringPost(postId);

        if(!isAllowedToMakePostAction(actor, toRemove)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostAction(postId, actor, "remove"));
        }
        volunteeringPostRepository.removeVolunteeringPost(postId);
    }

    public void editVolunteeringPost(int postId, String title, String description, String actor) {
        //TODO: check if user exists and logged in

        VolunteeringPost toEdit = volunteeringPostRepository.getVolunteeringPost(postId);

        if(!isAllowedToMakePostAction(actor, toEdit)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostAction(postId, actor, "edit"));
        }
        volunteeringPostRepository.editVolunteeringPost(postId, title, description);
    }

    public boolean doesExist(int postId) {
        try {
            volunteeringPostRepository.getVolunteeringPost(postId);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public VolunteeringPostDTO getVolunteeringPost(int postId) {
        VolunteeringPost post = volunteeringPostRepository.getVolunteeringPost(postId);
        return new VolunteeringPostDTO(post);
    }

    public List<VolunteeringPostDTO> getAllVolunteeringPosts() {
        List<VolunteeringPost> allPosts = volunteeringPostRepository.getAllVolunteeringPosts();
        return volunteeringPostRepository.getVolunteeringPostDTOs(allPosts);
    }

    public List<VolunteeringPostDTO> getOrganizationVolunteeringPosts(int organizationId) {
        List<VolunteeringPost> orgPosts = volunteeringPostRepository.getOrganizationVolunteeringPosts(organizationId);
        return volunteeringPostRepository.getVolunteeringPostDTOs(orgPosts);
    }

    // TODO: remove when users facade is implemented
    private boolean isAdmin(String username) {
        return false;
    }

}
