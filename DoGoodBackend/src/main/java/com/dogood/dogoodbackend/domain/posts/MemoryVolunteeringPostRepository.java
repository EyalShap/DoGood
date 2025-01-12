package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import com.dogood.dogoodbackend.utils.PostErrors;

import java.util.*;

public class MemoryVolunteeringPostRepository implements VolunteeringPostRepository {
    private Map<Integer, VolunteeringPost> posts;
    private int nextPostId;

    public MemoryVolunteeringPostRepository() {
        this.posts = new HashMap<>();
        this.nextPostId = 0;
    }

    @Override
    public int createVolunteeringPost(String title, String description, String posterUsername, int volunteeringId, int organizationId) {
        if(posts.containsKey(nextPostId)) {
            throw new IllegalArgumentException(PostErrors.makePostIdAlreadyExistsError(nextPostId));
        }

        VolunteeringPost post = new VolunteeringPost(nextPostId, title, description, posterUsername, volunteeringId, organizationId);
        posts.put(nextPostId, post);
        nextPostId++;
        return nextPostId - 1;
    }

    @Override
    public void removeVolunteeringPost(int postId) {
        if(!posts.containsKey(postId)) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }

        posts.remove(postId);
    }

    @Override
    public void removePostsByVolunteeringId(int volunteeringId) {
        for(VolunteeringPost post : posts.values()) {
            if(post.getVolunteeringId() == volunteeringId) {
                posts.remove(post);
            }
        }
    }

    @Override
    public void editVolunteeringPost(int postId, String title, String description) {
        VolunteeringPost toEdit = getVolunteeringPost(postId); // will throw exception if does not exist
        toEdit.edit(title, description);
    }

    @Override
    public void incNumOfPeopleRequestedToJoin(int postId) {
        VolunteeringPost toInc = getVolunteeringPost(postId); // will throw exception if does not exist
        toInc.incNumOfPeopleRequestedToJoin();
    }

    @Override
    public VolunteeringPost getVolunteeringPost(int postId) {
        if(!posts.containsKey(postId)) {
            throw new IllegalArgumentException(PostErrors.makePostIdDoesNotExistError(postId));
        }
        return posts.get(postId);
    }

    @Override
    public List<VolunteeringPost> getAllVolunteeringPosts() {
        return new ArrayList<>(posts.values());
    }

    @Override
    public List<VolunteeringPost> getOrganizationVolunteeringPosts(int organizationId) {
        List<VolunteeringPost> res = new ArrayList<>();
        for(VolunteeringPost post : posts.values()) {
            if(post.getOrganizationId() == organizationId) {
                res.add(post);
            }
        }
        return res;
    }

    @Override
    public int getVolunteeringIdByPostId(int postId) {
        VolunteeringPost post = getVolunteeringPost(postId);
        return post.getVolunteeringId();
    }

}
