package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.posts.PostDTO;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteerPostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.PastExperience;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PostService {
    private PostsFacade postsFacade;
    private AuthFacade authFacade;
    private UsersFacade usersFacade;

    @Autowired
    public PostService(FacadeManager facadeManager){
        this.postsFacade = facadeManager.getPostsFacade();
        this.authFacade = facadeManager.getAuthFacade();
        this.usersFacade = facadeManager.getUsersFacade();
    }

    private void checkToken(String token, String username){
        if(!authFacade.getNameFromToken(token).equals(username)){
            throw new IllegalArgumentException("Invalid token");
        }
        if (usersFacade.isBanned(username)) {
            throw new IllegalArgumentException("Banned user.");
        }
    }

    public Response<Integer> createVolunteeringPost(String token, String title, String description, String actor, int volunteeringId) {
        try {
            checkToken(token,actor);
            int postId = postsFacade.createVolunteeringPost(title, description, actor, volunteeringId);
            return Response.createResponse(postId);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeVolunteeringPost(String token, int postId, String actor) {
        try {
            checkToken(token,actor);
            postsFacade.removeVolunteeringPost(postId, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> editVolunteeringPost(String token, int postId, String title, String description, String actor) {
        try {
            checkToken(token,actor);
            postsFacade.editVolunteeringPost(postId, title, description, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<VolunteeringPostDTO> getVolunteeringPost(String token, int postId, String actor) {
        try {
            checkToken(token,actor);
            VolunteeringPostDTO post = postsFacade.getVolunteeringPost(postId, actor);
            return Response.createResponse(post);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<VolunteerPostDTO> getVolunteerPost(String token, int postId, String actor) {
        try {
            checkToken(token,actor);
            VolunteerPostDTO post = postsFacade.getVolunteerPost(postId, actor);
            return Response.createResponse(post);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }


    public Response<List<VolunteeringPostDTO>> getAllVolunteeringPosts(String token, String actor) {
        try {
            checkToken(token,actor);
            List<VolunteeringPostDTO> posts = postsFacade.getAllVolunteeringPosts(actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> getOrganizationVolunteeringPosts(String token, int organizationId, String actor) {
        try {
            checkToken(token,actor);
            List<VolunteeringPostDTO> posts = postsFacade.getOrganizationVolunteeringPosts(organizationId, actor);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> joinVolunteeringRequest(String token, int postId, String actor, String freeText) {
        try {
            checkToken(token,actor);
            postsFacade.joinVolunteeringRequest(postId, actor, freeText);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<? extends PostDTO>> searchByKeywords(String token, String search, String actor, List<PostDTO> allPosts, boolean volunteering) {
        try {
            checkToken(token,actor);
            List<? extends PostDTO> posts = postsFacade.searchByKeywords(search, actor, allPosts, volunteering);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByRelevance(String token, String actor, List<VolunteeringPostDTO> allPosts) {
        try {
            checkToken(token,actor);
            List<VolunteeringPostDTO> posts = postsFacade.sortByRelevance(actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> sortByPopularity(String token, String actor, List<VolunteeringPostDTO> allPosts) {
        try {
            checkToken(token,actor);
            List<VolunteeringPostDTO> posts = postsFacade.sortByPopularity(actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<PostDTO>> sortByPostingTime(String token, String actor, List<PostDTO> allPosts) {
        try {
            checkToken(token,actor);
            List<PostDTO> posts = postsFacade.sortByPostingTime(actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<PostDTO>> sortByLastEditTime(String token, String actor, List<PostDTO> allPosts) {
        try {
            checkToken(token,actor);
            List<PostDTO> posts = postsFacade.sortByLastEditTime(actor, allPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteeringPostDTO>> filterVolunteeringPosts(String token, Set<String> categories, Set<String> skills, Set<String> cities, Set<String> organizationNames, Set<String> volunteeringNames, String actor, List<Integer> allPostIds, boolean isMyPosts) {
        try {
            checkToken(token,actor);
            List<VolunteeringPostDTO> posts = postsFacade.filterVolunteeringPosts(categories, skills, cities, organizationNames, volunteeringNames, actor, allPostIds, isMyPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteerPostDTO>> filterVolunteerPosts(String token, Set<String> categories, Set<String> skills, String actor, List<Integer> allPosts, boolean isMyPosts) {
        try {
            checkToken(token,actor);
            List<VolunteerPostDTO> posts = postsFacade.filterVolunteerPosts(categories, skills, actor, allPosts, isMyPosts);
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllPostsCategories(String token, String actor) {
        try {
            checkToken(token,actor);
            List<String> categories = postsFacade.getAllPostsCategories();
            return Response.createResponse(categories);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllPostsSkills(String token, String actor) {
        try {
            checkToken(token,actor);
            List<String> skills = postsFacade.getAllPostsSkills();
            return Response.createResponse(skills);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllVolunteerPostsCategories(String token, String actor) {
        try {
            checkToken(token,actor);
            List<String> categories = postsFacade.getAllVolunteerPostsCategories();
            return Response.createResponse(categories);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllVolunteerPostsSkills(String token, String actor) {
        try {
            checkToken(token,actor);
            List<String> skills = postsFacade.getAllVolunteerPostsSkills();
            return Response.createResponse(skills);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllPostsCities(String token, String actor) {
        try {
            checkToken(token,actor);
            List<String> cities = postsFacade.getAllPostsCities();
            return Response.createResponse(cities);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllOrganizationNames(String token, String actor) {
        try {
            checkToken(token,actor);
            List<String> orgNames = postsFacade.getAllPostsOrganizations();
            return Response.createResponse(orgNames);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllVolunteeringNames(String token, String actor) {
        try {
            checkToken(token,actor);
            List<String> volNames = postsFacade.getAllPostsVolunteerings();
            return Response.createResponse(volNames);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<PastExperience>> getPostPastExperiences(String token, String actor, int postId) {
        try {
            checkToken(token,actor);
            List<PastExperience> pastExperiences = postsFacade.getPostPastExperiences(postId);
            return Response.createResponse(pastExperiences);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> getVolunteeringName(String token, String actor, int volunteeringId) {
        try {
            checkToken(token,actor);
            String name = postsFacade.getVolunteeringName(volunteeringId);
            return Response.createResponse(name, null);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getVolunteeringImages(String token, String actor, int volunteeringId) {
        try {
            checkToken(token,actor);
            List<String> images = postsFacade.getVolunteeringImages(volunteeringId);
            return Response.createResponse(images);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> createVolunteerPost(String token, String actor, String title, String description) {
        try {
            checkToken(token,actor);
            int id = postsFacade.createVolunteerPost(title, description, actor);
            return Response.createResponse(id);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeVolunteerPost(String token, String actor, int postId) {
        try {
            checkToken(token,actor);
            postsFacade.removeVolunteerPost(actor, postId);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> editVolunteerPost(String token, String actor, int postId, String title, String description) {
        try {
            checkToken(token,actor);
            postsFacade.editVolunteerPost(postId, title, description, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> sendAddRelatedUserRequest(String token, String actor, int postId, String username) {
        try {
            checkToken(token,actor);
            postsFacade.sendAddRelatedUserRequest(postId, username, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> handleAddRelatedUserRequest(String token, String actor, int postId, boolean approved) {
        try {
            checkToken(token,actor);
            postsFacade.handleAddRelatedUserRequest(postId, actor, approved);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeRelatedUser(String token, String actor, int postId, String username) {
        try {
            checkToken(token,actor);
            postsFacade.removeRelatedUser(postId, username, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> addImage(String token, String actor, int postId, String path) {
        try {
            checkToken(token,actor);
            postsFacade.addImage(postId, path, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> removeImage(String token, String actor, int postId, String path) {
        try {
            checkToken(token,actor);
            postsFacade.removeImage(postId, path, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<VolunteerPostDTO>> getAllVolunteerPosts(String token, String actor) {
        try {
            checkToken(token,actor);
            List<VolunteerPostDTO> posts = postsFacade.getAllVolunteerPosts();
            return Response.createResponse(posts);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<Request>> getUserRequests(String token, String actor) {
        try {
            checkToken(token, actor);
            List<Request> requests = postsFacade.getUserRequests(actor);
            return Response.createResponse(requests);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setPoster(String token, int postId, String actor, String newPoster) {
        try {
            checkToken(token, actor);
            postsFacade.setPoster(postId, actor, newPoster);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setVolunteerPostSkills(String token, int postId, String actor, List<String> skills) {
        try {
            checkToken(token, actor);
            postsFacade.setVolunteerPostSkills(postId, skills, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setVolunteerPostCategories(String token, int postId, String actor, List<String> categories) {
        try {
            checkToken(token, actor);
            postsFacade.setVolunteerPostCategories(postId, categories, actor);
            return Response.createResponse(true);
        }
        catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }
}
