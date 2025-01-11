package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.organizations.Request;
import com.dogood.dogoodbackend.domain.organizations.RequestKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestJPA extends JpaRepository<Request, RequestKey> {
    @Query("SELECT r FROM Request r WHERE r.assigneeUsername = :username")
    List<Request> findUserRequests(@Param("username") String username);
}
