package com.dogood.dogoodbackend.api.postrequests;

import com.dogood.dogoodbackend.domain.posts.PostDTO;

import java.util.List;

public class SearchPostRequest {
    private String actor;
    private List<PostDTO> allPosts;
    private boolean volunteering;

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

    public boolean isVolunteering() {
        return volunteering;
    }

    public void setVolunteering(boolean volunteering) {
        this.volunteering = volunteering;
    }
}
