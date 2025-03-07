package com.dogood.dogoodbackend.domain.requests;

import jakarta.persistence.Embeddable;

@Embeddable
public class RequestKey {
    private String assigneeUsername;
    private int objectId;
    private RequestObject requestObject;

    public RequestKey(String assigneeUsername, int objectId, RequestObject requestObject) {
        this.assigneeUsername = assigneeUsername;
        this.objectId = objectId;
        this.requestObject = requestObject;
    }

    public RequestKey() {
    }

    public String getAssigneeUsername() {
        return assigneeUsername;
    }

    public void setAssigneeUsername(String assigneeUsername) {
        this.assigneeUsername = assigneeUsername;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public RequestObject getRequestObject() {
        return requestObject;
    }

    public void setRequestObject(RequestObject requestObject) {
        this.requestObject = requestObject;
    }
}