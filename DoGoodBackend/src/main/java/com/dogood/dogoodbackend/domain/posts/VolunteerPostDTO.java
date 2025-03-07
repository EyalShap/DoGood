package com.dogood.dogoodbackend.domain.posts;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class VolunteerPostDTO extends PostDTO{
    private List<String> relatedUsers;
    private List<String> images;
    private List<String> skills;
    private List<String> categories;

    public VolunteerPostDTO() {

    }

    public VolunteerPostDTO(int id, String title, String description, LocalDateTime postedTime, LocalDateTime lastEditedTime, String posterUsername, int relevance, List<String> relatedUsers, List<String> images, Set<String> keywords, List<String> skills, List<String> categories) {
        super(id, title, description, postedTime, lastEditedTime, posterUsername, relevance, keywords);
        this.relatedUsers = relatedUsers;
        this.images = images;
        this.skills = skills;
        this.categories = categories;
    }

    public VolunteerPostDTO(VolunteerPost post) {
        super(post);
        this.relatedUsers = post.getRelatedUsers();
        this.images = post.getImages();
        this.skills = post.getSkills(null);
        this.categories = post.getCategories(null);
    }

    public List<String> getRelatedUsers() {
        return relatedUsers;
    }

    public void setRelatedUsers(List<String> relatedUsers) {
        this.relatedUsers = relatedUsers;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public List<String> getSkills(PostsFacade postsFacade) {
        return getSkills();
    }

    @Override
    public List<String> getCategories(PostsFacade postsFacade) {
        return getCategories();
    }
}
