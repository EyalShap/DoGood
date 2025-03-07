package com.dogood.dogoodbackend.domain.posts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "volunteering_posts")
public class VolunteeringPost extends Post{
    @Column(name = "volunteering_id")
    private int volunteeringId;

    @Column(name = "organization_id")
    private int organizationId;

    @Column(name = "post_num_of_join_requests")
    private int numOfPeopleRequestedToJoin; //this is to calculate popularity, TODO: something better in beta version

    public VolunteeringPost(int id, String title, String description, Set<String> keywords, String posterUsername, int volunteeringId, int organizationId) {
        super(id, title, description, posterUsername, keywords);
        this.volunteeringId = volunteeringId;
        this.organizationId = organizationId;
        this.numOfPeopleRequestedToJoin = 0;
    }

    public VolunteeringPost(String title, String description, Set<String> keywords, String posterUsername, int volunteeringId, int organizationId) {
        super(title, description, posterUsername, keywords);
        this.volunteeringId = volunteeringId;
        this.organizationId = organizationId;
        this.numOfPeopleRequestedToJoin = 0;
    }

    public VolunteeringPost() {}

    public List<String> getSkills(PostsFacade postsFacade) {
        return postsFacade.getVolunteeringSkills(volunteeringId);
    }

    public List<String> getCategories(PostsFacade postsFacade) {
        return postsFacade.getVolunteeringCategories(volunteeringId);
    }

    public void incNumOfPeopleRequestedToJoin() {
        numOfPeopleRequestedToJoin++;
    }

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public int getNumOfPeopleRequestedToJoin() {
        return numOfPeopleRequestedToJoin;
    }

    public int evaluatePopularity() {
        /*if(Glinda) {
            return Integer.MAX_VALUE;
        }
        // will be betaba in beta version
        else if(Elphaba) {
            return Integer.MIN_VALUE;
        }
        return 0; */

        return numOfPeopleRequestedToJoin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VolunteeringPost that = (VolunteeringPost) o;
        return volunteeringId == that.volunteeringId && organizationId == that.organizationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(volunteeringId, organizationId);
    }
}
