package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;
import com.dogood.dogoodbackend.service.PostService;
import com.dogood.dogoodbackend.service.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
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
    public Response<Boolean> removeVolunteeringPost(@RequestBody GeneralRequest removeVolunteeringPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        int postId = removeVolunteeringPostRequest.getId();
        String actor = removeVolunteeringPostRequest.getActor();
        return postService.removeVolunteeringPost(token, postId, actor);
    }

    @PutMapping("/editVolunteeringPost")
    public Response<Boolean> editVolunteeringPost(@RequestParam int postId, @RequestBody CreateVolunteeringPostRequest createVolunteeringPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        String title = createVolunteeringPostRequest.getTitle();
        String description = createVolunteeringPostRequest.getDescription();
        String actor = createVolunteeringPostRequest.getActor();
        int volunteeringId = createVolunteeringPostRequest.getVolunteeringId();
        return postService.editVolunteeringPost(token, postId, title, description, actor);
    }

    @GetMapping("/getVolunteeringPost")
    public Response<VolunteeringPostDTO> getVolunteeringPost(@RequestBody GeneralRequest getPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = getPostRequest.getActor();
        int postId = getPostRequest.getId();
        return postService.getVolunteeringPost(token, postId, actor);
    }

    @GetMapping("/getAllVolunteeringPosts")
    public Response<List<VolunteeringPostDTO>> getAllVolunteeringPosts(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.getAllVolunteeringPosts(token, actor);
    }

    @GetMapping("/getOrganizationVolunteeringPosts")
    public Response<List<VolunteeringPostDTO>> getOrganizationVolunteeringPosts(@RequestBody GeneralRequest getOrganizationPostsRequest, HttpServletRequest request) {
        String token = getToken(request);

        String actor = getOrganizationPostsRequest.getActor();
        int orgId = getOrganizationPostsRequest.getId();
        return postService.getOrganizationVolunteeringPosts(token, orgId, actor);
    }

    @PostMapping("/joinVolunteeringRequest")
    public Response<Boolean> joinVolunteeringRequest(@RequestBody GeneralRequest joinRequest, @RequestParam String freeText, HttpServletRequest request) {
        String token = getToken(request);

        int postId = joinRequest.getId();
        String actor = joinRequest.getActor();
        return postService.joinVolunteeringRequest(token, postId, actor, freeText);
    }

    @GetMapping("/searchByKeywords")
    public Response<List<VolunteeringPostDTO>> searchByKeywords(@RequestBody SearchPostRequest searchPostRequest, HttpServletRequest request) {
        String token = getToken(request);

        String search = searchPostRequest.getSearch();
        String actor = searchPostRequest.getActor();
        return postService.searchByKeywords(token, search, actor);
    }

    @GetMapping("/sortByRelevance")
    public Response<List<VolunteeringPostDTO>> sortByRelevance(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByRelevance(token, actor);
    }

    @GetMapping("/sortByPopularity")
    public Response<List<VolunteeringPostDTO>> sortByPopularity(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByPopularity(token, actor);
    }

    @GetMapping("/sortByPostingTime")
    public Response<List<VolunteeringPostDTO>> sortByPostingTime(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByPostingTime(token, actor);
    }

    @GetMapping("/sortByLastEditTime")
    public Response<List<VolunteeringPostDTO>> sortByLastEditTime(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);

        return postService.sortByPostingTime(token, actor);
    }

    @GetMapping("/filterPosts")
    public Response<List<VolunteeringPostDTO>> filterPosts(@RequestBody FilterPostsRequest filterPostsRequest, HttpServletRequest request) {
        String token = getToken(request);

        Set<String> categories = filterPostsRequest.getCategories();
        Set<String> skills = filterPostsRequest.getSkills();
        Set<String> cities = filterPostsRequest.getCities();
        String actor = filterPostsRequest.getActor();
        return postService.filterPosts(token, categories, skills, cities, actor);
    }
}
