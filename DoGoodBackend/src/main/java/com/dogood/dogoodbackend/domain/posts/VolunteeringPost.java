package com.dogood.dogoodbackend.domain.posts;

import java.util.Objects;

public class VolunteeringPost extends Post{
    private int volunteeringId;
    private int organizationId;

    public VolunteeringPost(int id, String title, String description, String posterUsername, int volunteeringId, int organizationId) {
        super(id, title, description, posterUsername);
        this.volunteeringId = volunteeringId;
        this.organizationId = organizationId;
    }

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
