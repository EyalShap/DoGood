package com.dogood.dogoodbackend.domain.requests;


import com.dogood.dogoodbackend.domain.requests.RequestKey;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "requests")
@IdClass(RequestKey.class)
public class Request {
    @Id
    @Column(name = "assignee_username")
    private String assigneeUsername;

    @Column(name = "assigner_username")
    private String assignerUsername;

    @Id
    @Column(name = "object_id")
    private int objectId;

    @Id
    @Column(name = "request_object")
    private RequestObject requestObject;

    @Column(name = "date")
    private LocalDateTime date;

    public Request(String assigneeUsername, String assignerUsername, int objectId, RequestObject requestObject) {
        this.assigneeUsername = assigneeUsername;
        this.assignerUsername = assignerUsername;
        this.objectId = objectId;
        this.requestObject = requestObject;
        this.date = LocalDateTime.now();
    }

    public Request() {
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return objectId == request.objectId && Objects.equals(assigneeUsername, request.assigneeUsername) && Objects.equals(assignerUsername, request.assignerUsername) && requestObject == request.requestObject && Objects.equals(date, request.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assigneeUsername, assignerUsername, objectId, requestObject, date);
    }
}