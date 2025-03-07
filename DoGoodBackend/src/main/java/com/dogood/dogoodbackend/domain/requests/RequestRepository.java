package com.dogood.dogoodbackend.domain.requests;

import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RequestRepository {
    public Request createRequest(String assigneeUsername, String assignerUsername, int objectId, RequestObject requestObject);
    public void deleteRequest(String assigneeUsername, int objectId, RequestObject requestObject);
    public com.dogood.dogoodbackend.domain.requests.Request getRequest(String assigneeUsername, int objectId, RequestObject requestObject);
    public List<Request> getUserRequests(String username, RequestObject requestObject);
    public void removeObjectRequests(int objectId, RequestObject requestObject);
}
