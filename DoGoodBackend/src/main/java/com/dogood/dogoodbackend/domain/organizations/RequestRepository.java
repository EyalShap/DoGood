package com.dogood.dogoodbackend.domain.organizations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository {
    public Request createRequest(String assigneeUsername, String assignerUsername, int organizationId);
    public void deleteRequest(String assigneeUsername, int organizationId);
    public Request getRequest(String assigneeUsername, int organizationId);
    public List<Request> getUserRequests(String username);
    public void removeOrganizationRequests(int organizationId);
}
