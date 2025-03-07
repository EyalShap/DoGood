package com.dogood.dogoodbackend.api.postrequests;

import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;

import java.util.List;

public class VolunteeringPostSortRequest {
    private String actor;
    private List<VolunteeringPostDTO> allPosts;

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public List<VolunteeringPostDTO> getAllPosts() {
        return allPosts;
    }

    public void setAllPosts(List<VolunteeringPostDTO> allPosts) {
        this.allPosts = allPosts;
    }
}
