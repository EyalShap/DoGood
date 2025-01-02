package com.dogood.dogoodbackend.api.postrequests;

import com.dogood.dogoodbackend.domain.posts.VolunteeringPostDTO;

import java.util.List;
import java.util.Set;

public class FilterPostsRequest {
    private Set<String> categories;
    private Set<String> skills;
    private Set<String> cities;
    private Set<String> organizationNames;
    private Set<String> volunteeringNames;
    private String actor;
    private List<VolunteeringPostDTO> allPosts;

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

    public Set<String> getCities() {
        return cities;
    }

    public void setCities(Set<String> cities) {
        this.cities = cities;
    }

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

    public Set<String> getOrganizationNames() {
        return organizationNames;
    }

    public void setOrganizationNames(Set<String> organizationNames) {
        this.organizationNames = organizationNames;
    }

    public Set<String> getVolunteeringNames() {
        return volunteeringNames;
    }

    public void setVolunteeringNames(Set<String> volunteeringNames) {
        this.volunteeringNames = volunteeringNames;
    }
}
