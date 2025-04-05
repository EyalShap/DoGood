package com.dogood.dogoodbackend.domain.posts;

import jakarta.persistence.*;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dogood.dogoodbackend.utils.ValidateFields.isValidText;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "post_id")
    private int id;

    @Column(name = "post_title")
    protected String title;

    @Column(name = "post_description")
    private String description;

    @Column(name = "post_posting_time")
    private LocalDateTime postedTime;

    @Column(name = "post_last_edit_time")
    private LocalDateTime lastEditedTime; // nicer in the UI

    @Column(name = "post_poster_username")
    protected String posterUsername;

    @Transient
    private int relevance;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_keywords", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "keyword")
    private Set<String> keywords;

    public Post(int id, String title, String description, String posterUsername, Set<String> keywords) {
        this.id = id;
        setFields(title, description, posterUsername, keywords);
    }

    public Post(String title, String description, String posterUsername, Set<String> keywords) {
        setFields(title, description, posterUsername, keywords);
    }

    private void setFields(String title, String description, String posterUsername, Set<String> keywords) {
        String isValidOrg = isValid(title, description);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.title = title;
        this.description = description;
        this.postedTime = LocalDateTime.now();
        this.lastEditedTime = this.postedTime;
        this.posterUsername = posterUsername;
        this.relevance = -1;
        this.keywords = keywords;
    }

    public Post() {}

    private String isValid(String title, String description) {
        StringBuilder res = new StringBuilder();
        if(id < 0) {
            res.append(String.format("Invalid id: %d.\n", id));
        }
        if(!isValidText(title, 2, 50)) {
            res.append(String.format("Invalid post title: %s.\n", title));
        }
        if(!isValidText(description, 2, 300)) {
            res.append(String.format("Invalid post description: %s.\n", description));
        }
        return res.toString();
    }

    public void edit(String title, String description, Set<String> keywords) {
        String isValidOrg = isValid(title, description);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.title = title;
        this.description = description;
        this.lastEditedTime = LocalDateTime.now();
        this.keywords = keywords;
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

    public String getPosterUsername() {
        return posterUsername;
    }

    public LocalDateTime getLastEditedTime() {
        return lastEditedTime;
    }

    public int getRelevance() {
        return relevance;
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


}
