package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.jparepos.VolunteerPostJPA;
import com.dogood.dogoodbackend.jparepos.VolunteeringPostJPA;
import com.dogood.dogoodbackend.utils.PostErrors;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional
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
    public void clear() {
        jpa.deleteAll();
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
    public void editVolunteerPost(int postId, String title, String description, Set<String> keywords, List<String> skills, List<String> categories) {
        VolunteerPost toEdit = getVolunteerPostForWrite(postId); // will throw exception if does not exist
        toEdit.edit(title, description, keywords);
        toEdit.setSkills(skills);
        toEdit.setCategories(categories);
        jpa.save(toEdit);
    }

    @Override
    public void addRelatedUser(int postId, String username) {
        VolunteerPost toAdd = getVolunteerPostForWrite(postId);
        toAdd.addUser(username);
        jpa.save(toAdd);
    }

    @Override
    public void removeRelatedUser(int postId, String username, String actor) {
        VolunteerPost toRemove = getVolunteerPostForWrite(postId);
        toRemove.removeUser(username, actor);
        jpa.save(toRemove);
    }

    @Override
    public void addImage(int postId, String path, String actor) {
        VolunteerPost toAdd = getVolunteerPostForWrite(postId);
        toAdd.addImage(actor, path);
        jpa.save(toAdd);
    }

    @Override
    public void removeImage(int postId, String path, String actor) {
        VolunteerPost toRemove = getVolunteerPostForWrite(postId);
        toRemove.removeImage(actor, path);
        jpa.save(toRemove);
    }

    @Override
    public void setPoster(int postId, String actor, String newPoster) {
        VolunteerPost toSet = getVolunteerPostForWrite(postId);
        toSet.setPoster(actor, newPoster);
        jpa.save(toSet);
    }

    @Override
    public VolunteerPost getVolunteerPostForRead(int postId) {
        Optional<VolunteerPost> post = jpa.findById(postId);
        if(!post.isPresent()) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }

        return post.get();    }

    @Override
    public VolunteerPost getVolunteerPostForWrite(int postId) {
        Optional<VolunteerPost> post = jpa.findByIdForUpdate(postId);
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
