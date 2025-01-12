package com.dogood.dogoodbackend.domain.organizations;



import com.dogood.dogoodbackend.utils.OrganizationErrors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryRequestRepository implements RequestRepository{
    private Map<String, Map<Integer, Request>> requests;

    public MemoryRequestRepository() {
        this.requests = new HashMap<>();
    }

    private boolean requestExists(String assigneeUsername, int organizationId) {
        return requests.containsKey(assigneeUsername) && requests.get(assigneeUsername).containsKey(organizationId);
    }

    @Override
    public Request createRequest(String assigneeUsername, String assignerUsername, int organizationId) {
        if(requestExists(assigneeUsername, organizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeAssignManagerRequestAlreadyExistsError(assigneeUsername, organizationId));
        }

        Request newRequest = new Request(assigneeUsername, assignerUsername, organizationId);

        requests.putIfAbsent(assigneeUsername, new HashMap<>());
        requests.get(assigneeUsername).put(organizationId, newRequest);
        return newRequest;
    }

    @Override
    public void deleteRequest(String assigneeUsername, int organizationId) {
        if(!requestExists(assigneeUsername, organizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeAssignManagerRequestDoesNotExistError(assigneeUsername, organizationId));
        }

        requests.get(assigneeUsername).remove(organizationId);
        if(requests.get(assigneeUsername).isEmpty()) {
            requests.remove(assigneeUsername);
        }
    }

    @Override
    public Request getRequest(String assigneeUsername, int organizationId) {
        if(!requestExists(assigneeUsername, organizationId)) {
            throw new IllegalArgumentException(OrganizationErrors.makeAssignManagerRequestDoesNotExistError(assigneeUsername, organizationId));
        }

        return requests.get(assigneeUsername).get(organizationId);
    }

    @Override
    public List<Request> getUserRequests(String username) {
        if(requests.containsKey(username)) {
            return new ArrayList<>(requests.get(username).values());
        }
        return new ArrayList<>();
    }

    @Override
    public void removeOrganizationRequests(int organizationId) {
        for(String username : requests.keySet()) {
            Map<Integer, Request> userRequests = requests.get(username);
            if(userRequests.containsKey(organizationId)) {
                userRequests.remove(organizationId);
            }
            if(userRequests.isEmpty()) {
                requests.remove(username);
            }
        }
    }
}
