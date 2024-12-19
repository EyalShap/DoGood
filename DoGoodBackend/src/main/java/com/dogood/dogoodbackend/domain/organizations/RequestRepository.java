package com.dogood.dogoodbackend.domain.organizations;

import java.util.List;

public interface RequestRepository {
    public Request createRequest(String assigneeUsername, String assignerUsername, int organizationId);
    public void deleteRequest(String assigneeUsername, int organizationId);
    public Request getRequest(String assigneeUsername, int organizationId);
    public List<Request> getUserRequests(String username);
}
