package com.dogood.dogoodbackend.jparepos;
import com.dogood.dogoodbackend.domain.requests.Request;
import com.dogood.dogoodbackend.domain.requests.RequestKey;
import com.dogood.dogoodbackend.domain.requests.RequestObject;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestJPA extends JpaRepository<Request, RequestKey> {
    List<Request> findByAssigneeUsernameAndRequestObject(String assigneeUsername, RequestObject requestObject);

    @Modifying
    @Transactional
    Long deleteByObjectIdAndRequestObject(int objectId, RequestObject requestObject);
}
