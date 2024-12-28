package com.dogood.dogoodbackend.domain.posts;

import java.time.LocalTime;

public class VolunteeringPostDTO extends PostDTO{
    private int volunteeringId;
    private int organizationId;

    public VolunteeringPostDTO(int id, String title, String description, LocalTime postedTime, LocalTime lastEditedTime, String posterUsername, int numOfPeopleRequestedToJoin, int relevance, int volunteeringId, int organizationId) {
        super(id, title, description, postedTime, lastEditedTime, posterUsername,numOfPeopleRequestedToJoin, relevance);
        this.volunteeringId = volunteeringId;
        this.organizationId = organizationId;
    }

    public VolunteeringPostDTO(VolunteeringPost post) {
        super(post);
        this.volunteeringId = post.getVolunteeringId();
        this.organizationId = post.getOrganizationId();
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public int getOrganizationId() {
        return organizationId;
    }
}
