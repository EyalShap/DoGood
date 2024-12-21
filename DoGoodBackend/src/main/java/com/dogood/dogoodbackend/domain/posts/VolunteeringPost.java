package com.dogood.dogoodbackend.domain.posts;

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
}
