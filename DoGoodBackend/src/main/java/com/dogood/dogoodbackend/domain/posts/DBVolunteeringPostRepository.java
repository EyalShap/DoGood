package com.dogood.dogoodbackend.domain.posts;

import java.util.List;

public class DBVolunteeringPostRepository implements VolunteeringPostRepository{
    @Override
    public int createVolunteeringPost(String title, String description, String posterUsername, int volunteeringId, int organizationId) {
        //TODO
        return 0;
    }

    @Override
    public void removeVolunteeringPost(int postId) {
        //TODO
    }

    @Override
    public void editVolunteeringPost(int postId, String title, String description) {
        //TODO
    }

    @Override
    public VolunteeringPost getVolunteeringPost(int postId) {
        //TODO
        return null;
    }

    @Override
    public List<VolunteeringPost> getAllVolunteeringPosts() {
        //TODO
        return null;
    }

    @Override
    public List<VolunteeringPost> getOrganizationVolunteeringPosts(int organizationId) {
        return null;
    }
}
