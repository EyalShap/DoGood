package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.users.UserDTO;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dogood.dogoodbackend.utils.ValidateFields.isValidText;

public abstract class Post {
    private int id;
    private String title;
    private String description;
    private LocalTime postedTime;
    private LocalTime lastEditedTime; // nicer in the UI
    private String posterUsername;
    private int numOfPeopleRequestedToJoin; //this is to calculate popularity, TODO: something better in beta version

    public Post(int id, String title, String description, String posterUsername) {
        String isValidOrg = isValid(id, title, description);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.id = id;
        this.title = title;
        this.description = description;
        this.postedTime = LocalTime.now();
        this.lastEditedTime = this.postedTime;
        this.posterUsername = posterUsername;
        this.numOfPeopleRequestedToJoin = 0;
    }

    private String isValid(int id, String title, String description) {
        StringBuilder res = new StringBuilder();
        if(id < 0) {
            res.append(String.format("Invalid id: %d.\n", id));
        }
        if(!isValidText(title, 2, 50)) {
            res.append(String.format("Invalid post title: %s.\n", title));
        }
        if(!isValidText(description, 0, 300)) {
            res.append(String.format("Invalid post description: %s.\n", description));
        }
        return res.toString();
    }

    public void edit(String title, String description) {
        String isValidOrg = isValid(id, title, description);
        if(isValidOrg.length() > 0) {
            throw new IllegalArgumentException(isValidOrg);
        }

        this.title = title;
        this.description = description;
        this.lastEditedTime = LocalTime.now();
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

    public String getPosterUsername() {
        return posterUsername;
    }

    public LocalTime getLastEditedTime() {
        return lastEditedTime;
    }

    public void incNumOfPeopleRequestedToJoin() {
        numOfPeopleRequestedToJoin++;
    }

    public int evaluatePopularity() {
        /*if(Glinda) {
            return Integer.MAX_VALUE;
        }
        // will be betaba in beta version
        else if(Alphaba) {
            return Integer.MIN_VALUE;
        }
        return 0; */

        return numOfPeopleRequestedToJoin;
    }

}
