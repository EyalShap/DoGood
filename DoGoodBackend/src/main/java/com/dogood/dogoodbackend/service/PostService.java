package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.volunteerings.PastExperience;
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

    public Response<VolunteeringPostDTO> getVolunteeringPost(String token, int postId, String actor) {
        //TODO: check token

        try {
            VolunteeringPostDTO post = postsFacade.getVolunteeringPost(postId, actor);
            return Response.createResponse(post);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> getAllVolunteeringPosts(String token, String actor) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.getAllVolunteeringPosts(actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> getOrganizationVolunteeringPosts(String token, int organizationId, String actor) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.getOrganizationVolunteeringPosts(organizationId, actor);
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

    public Response<List<VolunteeringPostDTO>> searchByKeywords(String token, String search, String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.searchByKeywords(search, actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByRelevance(String token, String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.sortByRelevance(actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByPopularity(String token, String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.sortByPopularity(actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByPostingTime(String token, String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.sortByPostingTime(actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByLastEditTime(String token, String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.sortByLastEditTime(actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> filterPosts(String token, Set<String> categories, Set<String> skills, Set<String> cities, Set<String> organizationNames, Set<String> volunteeringNames, String actor, List<VolunteeringPostDTO> allPosts) {
        //TODO: check token

        try {
            List<VolunteeringPostDTO> posts = postsFacade.filterPosts(categories, skills, cities, organizationNames, volunteeringNames, actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllPostsCategories(String token, String actor) {
        //TODO: check token

        try {
            List<String> categories = postsFacade.getAllPostsCategories();
            return Response.createResponse(categories);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllPostsSkills(String token, String actor) {
        //TODO: check token

        try {
            List<String> skills = postsFacade.getAllPostsSkills();
            return Response.createResponse(skills);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllPostsCities(String token, String actor) {
        //TODO: check token

        try {
            List<String> cities = postsFacade.getAllPostsCities();
            return Response.createResponse(cities);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllOrganizationNames(String token, String actor) {
        //TODO: check token

        try {
            List<String> orgNames = postsFacade.getAllPostsOrganizations();
            return Response.createResponse(orgNames);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllVolunteeringNames(String token, String actor) {
        //TODO: check token

        try {
            List<String> volNames = postsFacade.getAllPostsVolunteerings();
            return Response.createResponse(volNames);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<PastExperience>> getPostPastExperiences(String token, String actor, int postId) {
        //TODO: check token

        try {
            List<PastExperience> pastExperiences = postsFacade.getPostPastExperiences(postId);
            return Response.createResponse(pastExperiences);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> getVolunteeringName(String token, String actor, int volunteeringId) {
        //TODO: check token

        try {
            String name = postsFacade.getVolunteeringName(volunteeringId);
            return Response.createResponse(name, null);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }
}
