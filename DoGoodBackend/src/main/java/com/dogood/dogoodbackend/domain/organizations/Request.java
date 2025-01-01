package com.dogood.dogoodbackend.domain.organizations;

public class Request {
    private String assigneeUsername;
    private String assignerUsername;
    private int organizationId;

    public Request(String assigneeUsername, String assignerUsername, int organizationId) {
        this.assigneeUsername = assigneeUsername;
        this.assignerUsername = assignerUsername;
        this.organizationId = organizationId;
    }

    public String getAssignerUsername() {
        return assignerUsername;
    }

    public String getAssigneeUsername() {
        return assigneeUsername;
    }

    public void setAssigneeUsername(String assigneeUsername) {
        this.assigneeUsername = assigneeUsername;
    }

    public void setAssignerUsername(String assignerUsername) {
        this.assignerUsername = assignerUsername;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
}
