package com.dogood.dogoodbackend.domain.posts;

import java.time.LocalTime;

public abstract class PostDTO {
    private int id;
    private String title;
    private String description;
    private LocalTime postedTime;
    private LocalTime lastEditedTime; // nicer in the UI
    private String posterUsername;
    private int numOfPeopleRequestedToJoin;
    private int relevance;

    public PostDTO(int id, String title, String description, LocalTime postedTime, LocalTime lastEditedTime, String posterUsername, int numOfPeopleRequestedToJoin, int relevance) {
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

    public LocalTime getPostedTime() {
        return postedTime;
    }

    public LocalTime getLastEditedTime() {
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
}
