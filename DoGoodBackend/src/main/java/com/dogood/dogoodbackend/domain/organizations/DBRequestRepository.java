package com.dogood.dogoodbackend.domain.organizations;

import com.dogood.dogoodbackend.jparepos.RequestJPA;
import com.dogood.dogoodbackend.utils.OrganizationErrors;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public class DBRequestRepository implements RequestRepository{
    private RequestJPA jpa;

    public DBRequestRepository(RequestJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public Request createRequest(String assigneeUsername, String assignerUsername, int organizationId) {
        Request request = new Request(assigneeUsername, assignerUsername, organizationId);
        jpa.save(request);
        return request;
    }

    @Override
    public void deleteRequest(String assigneeUsername, int organizationId) {
        RequestKey key = new RequestKey(assigneeUsername, organizationId);
        if(!jpa.existsById(key)) {
            throw new IllegalArgumentException(OrganizationErrors.makeAssignManagerRequestDoesNotExistError(assigneeUsername, organizationId));
        }
        jpa.deleteById(key);
    }

    @Override
    public Request getRequest(String assigneeUsername, int organizationId) {
        RequestKey key = new RequestKey(assigneeUsername, organizationId);
        Optional<Request> request = jpa.findById(key);
        if(!request.isPresent()) {
            throw new IllegalArgumentException(OrganizationErrors.makeAssignManagerRequestDoesNotExistError(assigneeUsername, organizationId));
        }
        return request.get();
    }

    @Override
    public List<Request> getUserRequests(String username) {
        return jpa.findByAssigneeUsername(username);
    }

    @Override
    public void removeOrganizationRequests(int organizationId) {
        jpa.deleteByOrganizationId(organizationId);
    }
}
