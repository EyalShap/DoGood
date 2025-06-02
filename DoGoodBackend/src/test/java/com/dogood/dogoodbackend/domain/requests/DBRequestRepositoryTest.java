package com.dogood.dogoodbackend.domain.requests;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import com.dogood.dogoodbackend.domain.organizations.OrganizationRepository;
import com.dogood.dogoodbackend.jparepos.OrganizationJPA;
import com.dogood.dogoodbackend.jparepos.RequestJPA;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DBRequestRepositoryTest {
    @Autowired
    private ApplicationContext applicationContext;

    private RequestJPA jpa;
    private RequestRepository requestRepository;

    private final String assignee = "assignee";
    private final String assigner = "assigner";
    private final int objectId = 0;
    private final RequestObject requestObject = RequestObject.ORGANIZATION;

    @BeforeAll
    void createRepo() {
        this.jpa = applicationContext.getBean(RequestJPA.class);
        requestRepository = new DBRequestRepository(jpa);
    }

    @BeforeEach
    void setUp() {
        requestRepository.clear();
    }

    private void checkRequestDoesNotExist(String assignee, int objectId, RequestObject requestObject) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> requestRepository.getRequest(assignee, objectId, requestObject));
        String expectedError = String.format("A request to assign %s to %s with id %d does not exist.", assignee, requestObject.getString(), objectId);
        assertEquals(expectedError, exception.getMessage());
    }

    private void checkRequestExists(String assignee, int objectId, RequestObject requestObject) {
        assertDoesNotThrow(() -> requestRepository.getRequest(assignee, objectId, requestObject));
    }

    @Test
    void givenValidFields_whenCreateRequest_thenCreate() {
        checkRequestDoesNotExist(assignee, objectId, requestObject);
        requestRepository.createRequest(assignee, assigner, objectId ,requestObject);
        checkRequestExists(assignee, objectId, requestObject);
    }

    @Test
    void givenDoubleRequest_whenCreateRequest_thenThrowException() {
        checkRequestDoesNotExist(assignee, objectId, requestObject);
        requestRepository.createRequest(assignee, assigner, objectId ,requestObject);
        checkRequestExists(assignee, objectId, requestObject);
        assertEquals(1, requestRepository.getUserRequests(assignee, requestObject).size());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> requestRepository.createRequest(assignee, assigner, objectId ,requestObject));
        String expectedError = String.format("A request to assign %s to %s with id %d already exists.", assignee, requestObject.getString(), objectId);
        assertEquals(expectedError, exception.getMessage());

        assertEquals(1, requestRepository.getUserRequests(assignee, requestObject).size());
    }

    @Test
    void givenExistingRequest_whenDeleteRequest_thenDelete() {
        requestRepository.createRequest(assignee, assigner, objectId ,requestObject);
        checkRequestExists(assignee, objectId, requestObject);
        assertEquals(1, requestRepository.getUserRequests(assignee, requestObject).size());

        requestRepository.deleteRequest(assignee, objectId, requestObject);

        checkRequestDoesNotExist(assignee, objectId, requestObject);
        assertEquals(0, requestRepository.getUserRequests(assignee, requestObject).size());
    }

    @Test
    void givenNonExistingRequest_whenDeleteRequest_thenThrowException() {
        String newUser = "newUser";
        int newObjectId = 1;
        RequestObject newRequestObject = RequestObject.VOLUNTEER_POST;

        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> requestRepository.deleteRequest(newUser, objectId ,requestObject));
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> requestRepository.deleteRequest(assignee, newObjectId ,requestObject));
        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> requestRepository.deleteRequest(assignee, objectId ,newRequestObject));

        String expectedError1 = String.format("A request to assign %s to %s with id %d does not exist.", newUser, requestObject.getString(), objectId);
        String expectedError2 = String.format("A request to assign %s to %s with id %d does not exist.", assignee, requestObject.getString(), newObjectId);
        String expectedError3 = String.format("A request to assign %s to %s with id %d does not exist.", assignee, newRequestObject.getString(), objectId);

        assertEquals(expectedError1, exception1.getMessage());
        assertEquals(expectedError2, exception2.getMessage());
        assertEquals(expectedError3, exception3.getMessage());
    }

    private boolean myAssertEquals(Request r1, Request r2) {
        return r1.getAssigneeUsername().equals(r2.getAssigneeUsername()) &&
                r1.getAssignerUsername().equals(r2.getAssignerUsername()) &&
                r1.getRequestObject() == r2.getRequestObject() &&
                r1.getObjectId() == r2.getObjectId();
    }

    private boolean myAssertEquals(List<Request> s1, List<Request> s2) {
        if(s1.size() != s2.size())
            return false;

        for(int i = 0; i < s1.size(); i++) {
            if(!myAssertEquals(s1.get(i), s2.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Test
    void givenExistingRequest_whenGetRequest_thenReturnRequest() {
        requestRepository.createRequest(assignee, assigner, objectId ,requestObject);
        Request expected = new Request(assignee, assigner, objectId, requestObject);
        Request returned = requestRepository.getRequest(assignee, objectId, requestObject);
        myAssertEquals(expected, returned);
    }

    @Test
    void givenNonExistingRequest_whenGetRequest_thenThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> requestRepository.getRequest(assignee, objectId, requestObject));
        String expectedError = String.format("A request to assign %s to %s with id %d does not exist.", assignee, requestObject.getString(), objectId);
        assertEquals(expectedError, exception.getMessage());
    }

    @Test
    void getUserRequests() {
        requestRepository.createRequest(assignee, assigner, objectId ,requestObject);
        requestRepository.createRequest(assignee, assigner, objectId + 1 ,requestObject);
        requestRepository.createRequest(assignee, assigner, objectId + 2 ,RequestObject.VOLUNTEER_POST);

        Request request1 = new Request(assignee, assigner, objectId, requestObject);
        Request request2 = new Request(assignee, assigner, objectId + 1, requestObject);
        Request request3 = new Request(assignee, assigner, objectId + 2, RequestObject.VOLUNTEER_POST);

        List<Request> res1 = requestRepository.getUserRequests(assignee, requestObject);
        List<Request> expected1 = List.of(request1, request2);
        myAssertEquals(expected1, res1);

        List<Request> res2 = requestRepository.getUserRequests(assignee, RequestObject.VOLUNTEER_POST);
        List<Request> expected2 = List.of(request3);
        myAssertEquals(expected2, res2);

        List<Request> res3 = requestRepository.getUserRequests(assigner, RequestObject.VOLUNTEER_POST);
        List<Request> expected3 = List.of();
        myAssertEquals(expected3, res3);
    }

    @Test
    void removeObjectRequests() {
        requestRepository.createRequest(assignee, assigner, objectId ,requestObject);
        requestRepository.createRequest(assigner, assignee, objectId ,requestObject);
        requestRepository.createRequest(assignee, assignee, objectId + 1 ,requestObject);

        Request request1 = new Request(assignee, assigner, objectId, requestObject);
        Request request2 = new Request(assigner, assignee, objectId, requestObject);
        Request request3 = new Request(assignee, assignee, objectId + 1 ,requestObject);

        List<Request> resAssigneeBefore = requestRepository.getUserRequests(assignee, requestObject);
        List<Request> expectedAssigneeBefore = List.of(request1, request3);
        myAssertEquals(expectedAssigneeBefore, resAssigneeBefore);

        List<Request> resAssignerBefore = requestRepository.getUserRequests(assigner, requestObject);
        List<Request> expectedAssignerBefore = List.of(request2);
        myAssertEquals(expectedAssignerBefore, resAssignerBefore);

        requestRepository.removeObjectRequests(objectId, requestObject);

        List<Request> resAssigneeAfter = requestRepository.getUserRequests(assignee, requestObject);
        List<Request> expectedAssigneeAfter = List.of(request3);
        myAssertEquals(expectedAssigneeAfter, resAssigneeAfter);

        List<Request> resAssignerAfter = requestRepository.getUserRequests(assigner, requestObject);
        List<Request> expectedAssignerAfter = List.of();
        myAssertEquals(expectedAssignerAfter, resAssignerAfter);
    }
}