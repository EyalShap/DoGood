package com.dogood.dogoodbackend.domain.organizations;

import java.util.List;

public class DBRequestRepository implements RequestRepository{
    @Override
    public Request createRequest(String assigneeUsername, String assignerUsername, int organizationId) {
        //TODO
        return null;
    }

    @Override
    public void deleteRequest(String assigneeUsername, int organizationId) {
        //TODO
    }

    @Override
    public Request getRequest(String assigneeUsername, int organizationId) {
        //TODO
        return null;
    }

    @Override
    public List<Request> getUserRequests(String username) {
        //TODO
        return null;
    }
}
