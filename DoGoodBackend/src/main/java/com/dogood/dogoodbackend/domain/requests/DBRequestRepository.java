package com.dogood.dogoodbackend.domain.requests;

import com.dogood.dogoodbackend.jparepos.RequestJPA;

import java.util.List;
import java.util.Optional;

public class DBRequestRepository implements RequestRepository{
    private RequestJPA jpa;

    public DBRequestRepository(RequestJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public Request createRequest(String assigneeUsername, String assignerUsername, int objectId, RequestObject requestObject) {
        Request request = new Request(assigneeUsername, assignerUsername, objectId, requestObject);
        jpa.save(request);
        return request;
    }

    @Override
    public void deleteRequest(String assigneeUsername, int objectId, RequestObject requestObject) {
        RequestKey key = new RequestKey(assigneeUsername, objectId, requestObject);
        if(!jpa.existsById(key)) {
            throw new IllegalArgumentException(String.format("A request to assign %s to %s with id %d does not exist.", assigneeUsername, requestObject.getString(), objectId));
        }
        jpa.deleteById(key);
    }

    @Override
    public Request getRequest(String assigneeUsername, int objectId, RequestObject requestObject) {
        RequestKey key = new RequestKey(assigneeUsername, objectId, requestObject);
        Optional<Request> request = jpa.findById(key);
        if(!request.isPresent()) {
            throw new IllegalArgumentException(String.format("A request to assign %s to %s with id %d does not exist.", assigneeUsername, requestObject.getString(), objectId));
        }
        return request.get();
    }

    @Override
    public List<Request> getUserRequests(String username, RequestObject requestObject) {
        return jpa.findByAssigneeUsernameAndRequestObject(username, requestObject);
    }

    @Override
    public void removeObjectRequests(int objectId, RequestObject requestObject) {
        jpa.deleteByObjectIdAndRequestObject(objectId, requestObject);
    }
}
