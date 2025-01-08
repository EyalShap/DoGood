package com.dogood.dogoodbackend.domain.posts;

import java.time.LocalDateTime;
import java.time.LocalTime;

public abstract class PostDTO {
    private int id;
    private String title;
    private String description;
    private LocalDateTime postedTime;
    private LocalDateTime lastEditedTime; // nicer in the UI
    private String posterUsername;
    private int numOfPeopleRequestedToJoin;
    private int relevance;

    public PostDTO() {

    }

    public PostDTO(int id, String title, String description, LocalDateTime postedTime, LocalDateTime lastEditedTime, String posterUsername, int numOfPeopleRequestedToJoin, int relevance) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.postedTime = postedTime;
        this.lastEditedTime = lastEditedTime;
        this.posterUsername = posterUsername;
        this.numOfPeopleRequestedToJoin = numOfPeopleRequestedToJoin;
        this.relevance = relevance;
    }

    public PostDTO(Post post) {
        this.id = post.getId();
        this.title =post.getTitle();
        this.description = post.getDescription();
        this.postedTime = post.getPostedTime();
        this.lastEditedTime = post.getLastEditedTime();
        this.posterUsername = post.getPosterUsername();
        this.numOfPeopleRequestedToJoin = post.getNumOfPeopleRequestedToJoin();
        this.relevance = post.getRelevance();
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

    public int getNumOfPeopleRequestedToJoin() {
        return numOfPeopleRequestedToJoin;
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

    public void setNumOfPeopleRequestedToJoin(int numOfPeopleRequestedToJoin) {
        this.numOfPeopleRequestedToJoin = numOfPeopleRequestedToJoin;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }
}
