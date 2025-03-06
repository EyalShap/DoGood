package com.dogood.dogoodbackend.domain.posts;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public class VolunteeringPostDTO extends PostDTO{
    private int volunteeringId;
    private int organizationId;

    public VolunteeringPostDTO() {

    }

    public VolunteeringPostDTO(int id, String title, String description, LocalDateTime postedTime, LocalDateTime lastEditedTime, String posterUsername, int numOfPeopleRequestedToJoin, int relevance, int volunteeringId, int organizationId, Set<String> keywords) {
        super(id, title, description, postedTime, lastEditedTime, posterUsername,numOfPeopleRequestedToJoin, relevance, keywords);
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

    public void setVolunteeringId(int volunteeringId) {
        this.volunteeringId = volunteeringId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VolunteeringPostDTO that = (VolunteeringPostDTO) o;
        return volunteeringId == that.volunteeringId && organizationId == that.organizationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(volunteeringId, organizationId);
    }
}
