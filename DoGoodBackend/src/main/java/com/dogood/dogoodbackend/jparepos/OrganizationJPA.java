package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationJPA  extends JpaRepository<Organization, Integer> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Organization> findById(@Param("id") int id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Organization o WHERE o.id = :id")
    Optional<Organization> findByIdForUpdate(@Param("id") int id);

}
