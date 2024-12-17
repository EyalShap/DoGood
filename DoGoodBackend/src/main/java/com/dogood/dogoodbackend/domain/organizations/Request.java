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
}
