package com.dogood.dogoodbackend.domain.posts;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class VolunteeringPostDTO extends PostDTO{
    private int volunteeringId;
    private int organizationId;
    private int numOfPeopleRequestedToJoin;

    public VolunteeringPostDTO() {

    }

    public VolunteeringPostDTO(int id, String title, String description, LocalDateTime postedTime, LocalDateTime lastEditedTime, String posterUsername, int relevance, int volunteeringId, int organizationId, int numOfPeopleRequestedToJoin, Set<String> keywords) {
        super(id, title, description, postedTime, lastEditedTime, posterUsername, relevance, keywords);
        this.volunteeringId = volunteeringId;
        this.organizationId = organizationId;
        this.numOfPeopleRequestedToJoin = numOfPeopleRequestedToJoin;
    }

    public VolunteeringPostDTO(VolunteeringPost post) {
        super(post);
        this.volunteeringId = post.getVolunteeringId();
        this.organizationId = post.getOrganizationId();
        this.numOfPeopleRequestedToJoin = post.getNumOfPeopleRequestedToJoin();
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

    public int getNumOfPeopleRequestedToJoin() {
        return numOfPeopleRequestedToJoin;
    }

    public void setNumOfPeopleRequestedToJoin(int numOfPeopleRequestedToJoin) {
        this.numOfPeopleRequestedToJoin = numOfPeopleRequestedToJoin;
    }

    @Override
    public List<String> getSkills(PostsFacade postsFacade) {
        return postsFacade.getVolunteeringSkills(volunteeringId);
    }

    @Override
    public List<String> getCategories(PostsFacade postsFacade) {
        return postsFacade.getVolunteeringCategories(volunteeringId);
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
