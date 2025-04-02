package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.postrequests.*;
import com.dogood.dogoodbackend.domain.posts.PostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteerPostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.volunteerings.PastExperience;
import com.dogood.dogoodbackend.service.PostService;
import com.dogood.dogoodbackend.service.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
@CrossOrigin
@RequestMapping("/api/posts")
public class PostAPI {

    @Autowired
    private PostService postService;

    @PostMapping("/createVolunteeringPost")
    public Response<Integer> createVolunteeringPost(@RequestBody CreateVolunteeringPostRequest createVolunteeringPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        String title = createVolunteeringPostRequest.getTitle();
        String description = createVolunteeringPostRequest.getDescription();
        String actor = createVolunteeringPostRequest.getActor();
        int volunteeringId = createVolunteeringPostRequest.getVolunteeringId();
        return postService.createVolunteeringPost(token, title, description, actor, volunteeringId);
    }

    @DeleteMapping("/removeVolunteeringPost")
    public Response<Boolean> removeVolunteeringPost(@RequestParam int postId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.removeVolunteeringPost(token, postId, actor);
    }

    @PutMapping("/editVolunteeringPost")
    public Response<Boolean> editVolunteeringPost(@RequestParam int postId, @RequestBody CreateVolunteeringPostRequest createVolunteeringPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        String title = createVolunteeringPostRequest.getTitle();
        String description = createVolunteeringPostRequest.getDescription();
        String actor = createVolunteeringPostRequest.getActor();
        return postService.editVolunteeringPost(token, postId, title, description, actor);
    }

    @GetMapping("/getVolunteeringPost")
    public Response<VolunteeringPostDTO> getVolunteeringPost(@RequestParam int postId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getVolunteeringPost(token, postId, actor);
    }

    @GetMapping("/getVolunteerPost")
    public Response<VolunteerPostDTO> getVolunteerPost(@RequestParam int postId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getVolunteerPost(token, postId, actor);
    }

    @GetMapping("/getAllVolunteeringPosts")
    public Response<List<VolunteeringPostDTO>> getAllVolunteeringPosts(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllVolunteeringPosts(token, actor);
    }

    @GetMapping("/getOrganizationVolunteeringPosts")
    public Response<List<VolunteeringPostDTO>> getOrganizationVolunteeringPosts(@RequestParam int orgId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getOrganizationVolunteeringPosts(token, orgId, actor);
    }

    @PostMapping("/joinVolunteeringRequest")
    public Response<Boolean> joinVolunteeringRequest(@RequestParam String freeText, @RequestBody GeneralRequest joinRequest, HttpServletRequest request) {
        String token = getToken(request);

        int postId = joinRequest.getId();
        String actor = joinRequest.getActor();
        return postService.joinVolunteeringRequest(token, postId, actor, freeText);
    }

    @PostMapping("/searchByKeywords")
    public Response<List<? extends PostDTO>> searchByKeywords(@RequestParam String search, @RequestBody SearchPostRequest searchPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.searchByKeywords(token, search, searchPostRequest.getActor(), searchPostRequest.getAllPosts(), searchPostRequest.isVolunteering());
    }

    @PostMapping("/sortByRelevance")
    public Response<List<VolunteeringPostDTO>> sortByRelevance(@RequestBody VolunteeringPostSortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByRelevance(token, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/sortByPopularity")
    public Response<List<VolunteeringPostDTO>> sortByPopularity(@RequestBody VolunteeringPostSortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByPopularity(token, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/sortByPostingTime")
    public Response<List<PostDTO>> sortByPostingTime(@RequestBody PostSortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByPostingTime(token, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/sortByLastEditTime")
    public Response<List<PostDTO>> sortByLastEditTime(@RequestBody PostSortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByLastEditTime(token, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/filterVolunteeringPosts")
    public Response<List<VolunteeringPostDTO>> filterVolunteeringPosts(@RequestBody FilterVolunteeringPostsRequest filterPostsRequest, HttpServletRequest request) {
        String token = getToken(request);

        Set<String> categories = filterPostsRequest.getCategories();
        Set<String> skills = filterPostsRequest.getSkills();
        Set<String> cities = filterPostsRequest.getCities();
        Set<String> orgNames = filterPostsRequest.getOrganizationNames();
        Set<String> volNames = filterPostsRequest.getVolunteeringNames();
        String actor = filterPostsRequest.getActor();
        boolean isMyPosts = filterPostsRequest.getIsMyPosts();
        return postService.filterVolunteeringPosts(token, categories, skills, cities, orgNames, volNames, actor, filterPostsRequest.getAllPosts() ,isMyPosts);
    }

    @PostMapping("/filterVolunteerPosts")
    public Response<List<VolunteerPostDTO>> filterVolunteerPosts(@RequestBody FilterVolunteerPostsRequest filterPostsRequest, HttpServletRequest request) {
        String token = getToken(request);

        Set<String> categories = filterPostsRequest.getCategories();
        Set<String> skills = filterPostsRequest.getSkills();
        String actor = filterPostsRequest.getActor();
        boolean isMyPosts = filterPostsRequest.getIsMyPosts();
        return postService.filterVolunteerPosts(token, categories, skills, actor, filterPostsRequest.getAllPosts(), isMyPosts);
    }

    @GetMapping("/getAllPostsCategories")
    public Response<List<String>> getAllPostsCategories(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllPostsCategories(token, actor);
    }

    @GetMapping("/getAllPostsSkills")
    public Response<List<String>> getAllPostsSkills(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllPostsSkills(token, actor);
    }

    @GetMapping("/getAllVolunteerPostsCategories")
    public Response<List<String>> getAllVolunteerPostsCategories(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllVolunteerPostsCategories(token, actor);
    }

    @GetMapping("/getAllVolunteerPostsSkills")
    public Response<List<String>> getAllVolunteerPostsSkills(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllVolunteerPostsSkills(token, actor);
    }

    @GetMapping("/getAllPostsCities")
    public Response<List<String>> getAllPostsCities(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllPostsCities(token, actor);
    }

    @GetMapping("/getAllOrganizationNames")
    public Response<List<String>> getAllOrganizationNames(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllOrganizationNames(token, actor);
    }

    @GetMapping("/getAllVolunteeringNames")
    public Response<List<String>> getAllVolunteeringNames(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllVolunteeringNames(token, actor);
    }

    @GetMapping("/getPostPastExperiences")
    public Response<List<PastExperience>> getPostPastExperiences(@RequestParam int postId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getPostPastExperiences(token, actor, postId);
    }

    @GetMapping("/getVolunteeringName")
    public Response<String> getVolunteeringName(@RequestParam int volunteeringId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getVolunteeringName(token, actor, volunteeringId);
    }

    @GetMapping("/getVolunteeringImages")
    public Response<List<String>> getVolunteeringImages(@RequestParam int volunteeringId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getVolunteeringImages(token, actor, volunteeringId);
    }

    @PostMapping("/createVolunteerPost")
    public Response<Integer> createVolunteerPost(@RequestBody CreateVolunteeringPostRequest createVolunteerPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        String title = createVolunteerPostRequest.getTitle();
        String description = createVolunteerPostRequest.getDescription();
        String actor = createVolunteerPostRequest.getActor();
        return postService.createVolunteerPost(token, actor, title, description);
    }

    @DeleteMapping("/removeVolunteerPost")
    public Response<Boolean> removeVolunteerPost(@RequestParam int postId, @RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.removeVolunteerPost(token, actor, postId);
    }

    @PutMapping("/editVolunteerPost")
    public Response<Boolean> editVolunteerPost(@RequestParam int postId, @RequestBody CreateVolunteeringPostRequest createVolunteeringPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        String title = createVolunteeringPostRequest.getTitle();
        String description = createVolunteeringPostRequest.getDescription();
        String actor = createVolunteeringPostRequest.getActor();
        return postService.editVolunteerPost(token, actor, postId, title, description);
    }

    @PostMapping("/sendAddRelatedUserRequest")
    public Response<Boolean> sendAddRelatedUserRequest(@RequestBody GeneralRequest assignManagerRequest, @RequestParam String username, HttpServletRequest request) {
        String token = getToken(request);

        int postId = assignManagerRequest.getId();
        String actor = assignManagerRequest.getActor();
        return postService.sendAddRelatedUserRequest(token, actor, postId, username);
    }

    @PostMapping("/handleAddRelatedUserRequest")
    public Response<Boolean> handleAddRelatedUserRequest(@RequestBody GeneralRequest handleManagerRequest, @RequestParam boolean approved, HttpServletRequest request) {
        String token = getToken(request);

        int postId = handleManagerRequest.getId();
        String actor = handleManagerRequest.getActor();
        return postService.handleAddRelatedUserRequest(token, actor, postId, approved);
    }

    @DeleteMapping("/removeRelatedUser")
    public Response<Boolean> removeRelatedUser(@RequestParam int postId, @RequestParam String actor, @RequestParam String username, HttpServletRequest request) {
        String token = getToken(request);

        return postService.removeRelatedUser(token, actor, postId, username);
    }

    @PostMapping("/addImage")
    public Response<Boolean> addImage(@RequestParam int postId, @RequestParam String actor, @RequestBody String path, HttpServletRequest request) {
        String token = getToken(request);

        return postService.addImage(token, actor, postId, path);
    }

    @DeleteMapping("/removeImage")
    public Response<Boolean> removeImage(@RequestParam int postId, @RequestParam String actor, @RequestParam String path, HttpServletRequest request) {
        String token = getToken(request);

        return postService.removeImage(token, actor, postId, path);
    }

    @GetMapping("/getAllVolunteerPosts")
    public Response<List<VolunteerPostDTO>> getAllVolunteerPosts(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllVolunteerPosts(token, actor);
    }

    @GetMapping("/getVolunteerPostRequests")
    public Response<List<Request>> getVolunteerPostRequests(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getUserRequests(token, actor);
    }
}
