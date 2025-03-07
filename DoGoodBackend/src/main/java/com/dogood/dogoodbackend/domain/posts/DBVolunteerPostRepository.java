package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.jparepos.VolunteerPostJPA;
import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import com.dogood.dogoodbackend.utils.PostErrors;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DBVolunteerPostRepository implements VolunteerPostRepository{
    private VolunteerPostJPA jpa;

    public DBVolunteerPostRepository(VolunteerPostJPA jpa) {
        this.jpa = jpa;
    }

    public DBVolunteerPostRepository() {}

    public void setJPA(VolunteerPostJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public int createVolunteerPost(String title, String description, Set<String> keywords, String posterUsername, List<String> skills, List<String> categories) {
        VolunteerPost post = new VolunteerPost(title, description, keywords, posterUsername, skills, categories);
        jpa.save(post);
        return post.getId();
    }

    @Override
    public void removeVolunteerPost(int postId) {
        if(!jpa.existsById(postId)) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }
        jpa.deleteById(postId);
    }

    @Override
    public void editVolunteerPost(int postId, String title, String description, Set<String> keywords) {
        VolunteerPost toEdit = getVolunteerPost(postId); // will throw exception if does not exist
        toEdit.edit(title, description, keywords);
        jpa.save(toEdit);
    }

    @Override
    public void addRelatedUser(int postId, String username) {
        VolunteerPost toAdd = getVolunteerPost(postId);
        toAdd.addUser(username);
        jpa.save(toAdd);
    }

    @Override
    public void removeRelatedUser(int postId, String username, String actor) {
        VolunteerPost toRemove = getVolunteerPost(postId);
        toRemove.removeUser(username, actor);
        jpa.save(toRemove);
    }

    @Override
    public void addImage(int postId, String path, String actor) {
        VolunteerPost toAdd = getVolunteerPost(postId);
        toAdd.addImage(actor, path);
        jpa.save(toAdd);
    }

    @Override
    public void removeImage(int postId, String path, String actor) {
        VolunteerPost toRemove = getVolunteerPost(postId);
        toRemove.removeImage(actor, path);
        jpa.save(toRemove);
    }

    @Override
    public VolunteerPost getVolunteerPost(int postId) {
        Optional<VolunteerPost> post = jpa.findById(postId);
        if(!post.isPresent()) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }

        return post.get();
    }

    @Override
    public List<VolunteerPost> getAllVolunteerPosts() {
        return jpa.findAll();
    }
}
