package com.dogood.dogoodbackend.api;

public class CreateVolunteeringRequest {
    private int organizationId;
    private String volunteeringName;
    private String volunteeringDescription;
    private String actor;

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }

    public String getVolunteeringName() {
        return volunteeringName;
    }

    public void setVolunteeringName(String volunteeringName) {
        this.volunteeringName = volunteeringName;
    }

    public String getVolunteeringDescription() {
        return volunteeringDescription;
    }

    public void setVolunteeringDescription(String volunteeringDescription) {
        this.volunteeringDescription = volunteeringDescription;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}
