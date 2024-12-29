package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PostService {
    private PostsFacade postsFacade;

    @Autowired
    public PostService(FacadeManager facadeManager){
        this.postsFacade = facadeManager.getPostsFacade();
    }

    public Response<Integer> createVolunteeringPost(String token, String title, String description, String actor, int volunteeringId) {
        //TODO: check token

        try {
            int postId = postsFacade.createVolunteeringPost(title, description, actor, volunteeringId);
            return Response.createResponse(postId);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeVolunteeringPost(String token, int postId, String actor) {
        //TODO: check token

        try {
            postsFacade.removeVolunteeringPost(postId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> editVolunteeringPost(String token, int postId, String title, String description, String actor) {
        //TODO: check token

        try {
            postsFacade.editVolunteeringPost(postId, title, description, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<VolunteeringPostDTO> getVolunteeringPost(String token, int postId) {
        //TODO: check token

        try {
            VolunteeringPostDTO post = postsFacade.getVolunteeringPost(postId);
            return Response.createResponse(post);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> getAllVolunteeringPosts(String token) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.getAllVolunteeringPosts();
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> getOrganizationVolunteeringPosts(String token, int organizationId) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.getOrganizationVolunteeringPosts(organizationId);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> joinVolunteeringRequest(String token, int postId, String actor, String freeText) {
        //TODO: check token

        try {
            postsFacade.joinVolunteeringRequest(postId, actor, freeText);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> searchByKeywords(String token, String search, String actor) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.searchByKeywords(search, actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByRelevance(String token, String actor) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.sortByRelevance(actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByPopularity(String token, String actor) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.sortByPopularity(actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByPostingTime(String token, String actor) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.sortByPostingTime(actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByLastEditTime(String token, String actor) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.sortByLastEditTime(actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> filterPosts(String token, Set<String> categories, Set<String> skills, Set<String> cities, String actor) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.filterPosts(categories, skills, cities, actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }
}
