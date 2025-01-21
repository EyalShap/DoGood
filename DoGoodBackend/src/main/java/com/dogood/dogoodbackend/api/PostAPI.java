package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.postrequests.CreateVolunteeringPostRequest;
import com.dogood.dogoodbackend.api.postrequests.FilterPostsRequest;
import com.dogood.dogoodbackend.api.volunteeringrequests.SortRequest;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
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
    public Response<List<VolunteeringPostDTO>> searchByKeywords(@RequestParam String search, @RequestBody SortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.searchByKeywords(token, search, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/sortByRelevance")
    public Response<List<VolunteeringPostDTO>> sortByRelevance(@RequestBody SortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByRelevance(token, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/sortByPopularity")
    public Response<List<VolunteeringPostDTO>> sortByPopularity(@RequestBody SortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByPopularity(token, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/sortByPostingTime")
    public Response<List<VolunteeringPostDTO>> sortByPostingTime(@RequestBody SortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByPostingTime(token, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/sortByLastEditTime")
    public Response<List<VolunteeringPostDTO>> sortByLastEditTime(@RequestBody SortRequest sortRequest, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByPostingTime(token, sortRequest.getActor(), sortRequest.getAllPosts());
    }

    @PostMapping("/filterPosts")
    public Response<List<VolunteeringPostDTO>> filterPosts(@RequestBody FilterPostsRequest filterPostsRequest, HttpServletRequest request) {
        String token = getToken(request);

        Set<String> categories = filterPostsRequest.getCategories();
        Set<String> skills = filterPostsRequest.getSkills();
        Set<String> cities = filterPostsRequest.getCities();
        Set<String> orgNames = filterPostsRequest.getOrganizationNames();
        Set<String> volNames = filterPostsRequest.getVolunteeringNames();
        String actor = filterPostsRequest.getActor();
        return postService.filterPosts(token, categories, skills, cities, orgNames, volNames, actor, filterPostsRequest.getAllPosts());
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
}
