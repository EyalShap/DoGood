package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.UserVolunteerDateKT;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "assign_manager_requests")
@IdClass(RequestKey.class)
public class Request {
    @Id
    @Column(name = "assignee_username")
    private String assigneeUsername;

    @Column(name = "assigner_username")
    private String assignerUsername;

    @Id
    @Column(name = "organization_id")
    private int organizationId;

    public Request(String assigneeUsername, String assignerUsername, int organizationId) {
        this.assigneeUsername = assigneeUsername;
        this.assignerUsername = assignerUsername;
        this.organizationId = organizationId;
    }

    public Request() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return organizationId == request.organizationId && Objects.equals(assigneeUsername, request.assigneeUsername) && Objects.equals(assignerUsername, request.assignerUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assigneeUsername, assignerUsername, organizationId);
    }
}
