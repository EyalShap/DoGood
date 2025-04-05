package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.utils.PostErrors;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "volunteer_posts")
public class VolunteerPost extends Post {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_post_related_users", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "username")
    private List<String> relatedUsers;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_post_image_paths", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "path")
    private List<String> images;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_post_skills", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "skill")
    private List<String> skills;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_post_categories", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "categories")
    private List<String> categories;

    public VolunteerPost() {

    }

    public VolunteerPost(int id, String title, String description, Set<String> keywords, String posterUsername, List<String> skills,  List<String> categories) {
        super(id, title, description, posterUsername, keywords);
        setFields(skills, categories);
    }

    public VolunteerPost(String title, String description, Set<String> keywords, String posterUsername, List<String> skills,  List<String> categories) {
        super(title, description, posterUsername, keywords);
        setFields(skills, categories);
    }

    private void setFields(List<String> skills,  List<String> categories) {
        this.relatedUsers = new ArrayList<>();
        this.relatedUsers.add(posterUsername);
        this.images = new ArrayList<>();
        this.skills = skills;
        this.categories = categories;
    }

    public void addUser(String username) {
        if(relatedUsers.contains(username)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsRelatedToPost(username, this.title, true));
        }
        this.relatedUsers.add(username);
    }

    public void removeUser(String username, String actor) {
        if(!this.posterUsername.equals(actor)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, actor, "remove user from"));
        }
        if(this.posterUsername.equals(username)) {
            throw new IllegalArgumentException(PostErrors.makePosterCanNotBeRemovedFromPost(actor, this.title));
        }
        if(!relatedUsers.contains(username)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsRelatedToPost(actor, this.title, false));
        }
        this.relatedUsers.remove(username);
    }

    public void setPoster(String actor, String newPoster) {
        if(!this.posterUsername.equals(actor)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, actor, "set the poster user of"));
        }
        if(!relatedUsers.contains(newPoster)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsRelatedToPost(actor, this.title, false));
        }
        this.posterUsername = newPoster;
    }

    public void addImage(String actor, String path) {
        if(!relatedUsers.contains(actor)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, actor, "add image to"));
        }
        if(this.images.contains(path)) {
            throw new IllegalArgumentException(PostErrors.makeImagePathExists(path, actor, true));
        }
        if(path.charAt(0) == '\"') {
            int len = path.length();
            path = path.substring(1, len - 1);
        }
        this.images.add(path);
    }

    public void removeImage(String actor, String path) {
        if(!relatedUsers.contains(actor)) {
            throw new IllegalArgumentException(PostErrors.makeUserIsNotAllowedToMakePostActionError(this.title, actor, "remove image from"));
        }
        if(!this.images.contains(path)) {
            throw new IllegalArgumentException(PostErrors.makeImagePathExists(path, actor, false));
        }
        this.images.remove(path);
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

    public List<String> getSkills(PostsFacade postsFacade) {
        return this.skills;
    }

    public List<String> getCategories(PostsFacade postsFacade) {
        return this.categories;
    }

    public boolean hasRelatedUser(String username) {
        return username.equals(posterUsername) || relatedUsers.contains(username);
    }
}
