package com.dogood.dogoodbackend.domain.posts;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PostDTO {
    private int id;
    private String title;
    private String description;
    private LocalDateTime postedTime;
    private LocalDateTime lastEditedTime; // nicer in the UI
    private String posterUsername;
    private int relevance;
    private Set<String> keywords;

    public PostDTO() {

    }

    public PostDTO(int id, String title, String description, LocalDateTime postedTime, LocalDateTime lastEditedTime, String posterUsername, int relevance, Set<String> keywords) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.postedTime = postedTime;
        this.lastEditedTime = lastEditedTime;
        this.posterUsername = posterUsername;
        this.relevance = relevance;
        this.keywords = keywords;
    }

    public PostDTO(Post post) {
        this.id = post.getId();
        this.title =post.getTitle();
        this.description = post.getDescription();
        this.postedTime = post.getPostedTime();
        this.lastEditedTime = post.getLastEditedTime();
        this.posterUsername = post.getPosterUsername();
        this.relevance = post.getRelevance();
        this.keywords = post.getKeywords();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getPostedTime() {
        return postedTime;
    }

    public LocalDateTime getLastEditedTime() {
        return lastEditedTime;
    }

    public String getPosterUsername() {
        return posterUsername;
    }

    public int getRelevance() {
        return relevance;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPostedTime(LocalDateTime postedTime) {
        this.postedTime = postedTime;
    }

    public void setLastEditedTime(LocalDateTime lastEditedTime) {
        this.lastEditedTime = lastEditedTime;
    }

    public void setPosterUsername(String posterUsername) {
        this.posterUsername = posterUsername;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getSkills(PostsFacade postsFacade) {
        return new ArrayList<>();
    }

    public List<String> getCategories(PostsFacade postsFacade) {
        return new ArrayList<>();
    }
}
