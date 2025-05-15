package com.dogood.dogoodbackend.domain.posts;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VolunteerPostDTO that = (VolunteerPostDTO) o;
        return Objects.equals(new HashSet<>(relatedUsers), new HashSet<>(that.relatedUsers)) && Objects.equals(new HashSet<>(images), new HashSet<>(that.images)) && Objects.equals(new HashSet<>(skills), new HashSet<>(that.skills)) && Objects.equals(new HashSet<>(categories), new HashSet<>(that.categories));
    }

    @Override
    public int hashCode() {
        return Objects.hash(relatedUsers, images, skills, categories);
    }
}
