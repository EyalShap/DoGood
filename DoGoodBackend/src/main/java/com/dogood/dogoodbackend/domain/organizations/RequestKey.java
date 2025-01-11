package com.dogood.dogoodbackend.domain.organizations;

import jakarta.persistence.Embeddable;

@Embeddable
public class RequestKey {
    private String assigneeUsername;
    private int organizationId;

    public RequestKey(String assigneeUsername, int organizationId) {
        this.assigneeUsername = assigneeUsername;
        this.organizationId = organizationId;
    }

    public RequestKey() {
    }

    public String getAssigneeUsername() {
        return assigneeUsername;
    }

    public void setAssigneeUsername(String assigneeUsername) {
        this.assigneeUsername = assigneeUsername;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
}
