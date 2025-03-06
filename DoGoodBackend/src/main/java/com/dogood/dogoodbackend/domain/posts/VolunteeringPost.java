package com.dogood.dogoodbackend.domain.posts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "volunteering_posts")
public class VolunteeringPost extends Post{
    @Column(name = "volunteering_id")
    private int volunteeringId;

    @Column(name = "organization_id")
    private int organizationId;

    public VolunteeringPost(int id, String title, String description, Set<String> keywords, String posterUsername, int volunteeringId, int organizationId) {
        super(id, title, description, posterUsername, keywords);
        this.volunteeringId = volunteeringId;
        this.organizationId = organizationId;
    }

    public VolunteeringPost(String title, String description, Set<String> keywords, String posterUsername, int volunteeringId, int organizationId) {
        super(title, description, posterUsername, keywords);
        this.volunteeringId = volunteeringId;
        this.organizationId = organizationId;
    }

    public VolunteeringPost() {}

    public int getVolunteeringId() {
        return volunteeringId;
    }

    public int getOrganizationId() {
        return organizationId;
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
