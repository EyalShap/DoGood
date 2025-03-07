package com.dogood.dogoodbackend.api.postrequests;

import com.dogood.dogoodbackend.domain.posts.PostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;

import java.util.List;

public class PostSortRequest {
    private String actor;
    private List<PostDTO> allPosts;

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public List<PostDTO> getAllPosts() {
        return allPosts;
    }

    public void setAllPosts(List<PostDTO> allPosts) {
        this.allPosts = allPosts;
    }
}
