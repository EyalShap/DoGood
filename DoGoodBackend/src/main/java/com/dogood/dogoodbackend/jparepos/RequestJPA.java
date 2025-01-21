package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.organizations.Request;
import com.dogood.dogoodbackend.domain.organizations.RequestKey;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestJPA extends JpaRepository<Request, RequestKey> {
    List<Request> findByAssigneeUsername(String assigneeUsername);

    @Modifying
    @Transactional
    Long deleteByOrganizationId(int organizationId);
}
