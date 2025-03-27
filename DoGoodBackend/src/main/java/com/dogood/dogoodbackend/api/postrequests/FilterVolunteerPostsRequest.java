package com.dogood.dogoodbackend.api.postrequests;

import com.dogood.dogoodbackend.domain.posts.VolunteerPostDTO;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;

import java.util.List;
import java.util.Set;

public class FilterVolunteerPostsRequest {
    private Set<String> categories;
    private Set<String> skills;
    private String actor;
    private List<Integer> allPosts;

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Set<String> getSkills() {
        return skills;
    }

    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public List<Integer> getAllPosts() {
        return allPosts;
    }

    public void setAllPosts(List<Integer> allPosts) {
        this.allPosts = allPosts;
    }

}
