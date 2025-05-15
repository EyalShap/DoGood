package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.utils.PostErrors;

import java.util.*;

public class MemoryVolunteerPostRepository implements VolunteerPostRepository{
    private Map<Integer, VolunteerPost> posts;
    private int nextPostId;

    public MemoryVolunteerPostRepository() {
        clear();
    }

    @Override
    public void clear() {
        this.posts = new HashMap<>();
        this.nextPostId = 0;
    }

    @Override
    public int createVolunteerPost(String title, String description, Set<String> keywords, String posterUsername, List<String> skills, List<String> categories) {
        if(posts.containsKey(nextPostId)) {
            throw new IllegalArgumentException(PostErrors.makePostIdAlreadyExistsError(nextPostId));
        }

        VolunteerPost post = new VolunteerPost(nextPostId, title, description, keywords, posterUsername, skills, categories);
        posts.put(nextPostId, post);
        nextPostId++;
        return nextPostId - 1;
    }

    @Override
    public void removeVolunteerPost(int postId) {
        if(!posts.containsKey(postId)) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }

        posts.remove(postId);
    }

    @Override
    public void editVolunteerPost(int postId, String title, String description, Set<String> keywords, List<String> skills, List<String> categories) {
        VolunteerPost toEdit = getVolunteerPost(postId); // will throw exception if does not exist
        toEdit.edit(title, description, keywords);
        toEdit.setSkills(skills);
        toEdit.setCategories(categories);
    }

    @Override
    public void addRelatedUser(int postId, String username) {
        VolunteerPost toAdd = getVolunteerPost(postId);
        toAdd.addUser(username);
    }

    @Override
    public void removeRelatedUser(int postId, String username, String actor) {
        VolunteerPost toRemove = getVolunteerPost(postId);
        toRemove.removeUser(username, actor);
    }

    @Override
    public void addImage(int postId, String path, String actor) {
        VolunteerPost toAdd = getVolunteerPost(postId);
        toAdd.addImage(actor, path);
    }

    @Override
    public void removeImage(int postId, String path, String actor) {
        VolunteerPost toRemove = getVolunteerPost(postId);
        toRemove.removeImage(actor, path);
    }

    @Override
    public void setPoster(int postId, String actor, String newPoster) {
        VolunteerPost toSet = getVolunteerPost(postId);
        toSet.setPoster(actor, newPoster);
    }

    @Override
    public VolunteerPost getVolunteerPost(int postId) {
        if(!posts.containsKey(postId)) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }
        return posts.get(postId);
    }

    @Override
    public List<VolunteerPost> getAllVolunteerPosts() {
        return new ArrayList<>(posts.values());
    }

    @Override
    public List<VolunteerPostDTO> getVolunteerPostDTOs(List<VolunteerPost> posts) {
        return null;
    }

    @Override
    public List<VolunteerPostDTO> getVolunteerPostDTOs() {
        return null;
    }


}
