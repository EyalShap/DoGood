package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationJPA  extends JpaRepository<Organization, Integer> {
}
