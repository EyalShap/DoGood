package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.organizations.Organization;
import com.dogood.dogoodbackend.domain.posts.Post;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPost;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteeringPostJPA extends JpaRepository<VolunteeringPost, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM VolunteeringPost o WHERE o.id = :id")
    Optional<VolunteeringPost> findByIdForUpdate(@Param("id") int id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<VolunteeringPost> findById(@Param("id") int id);

    List<VolunteeringPost> findByVolunteeringId(int volunteeringId);

    List<VolunteeringPost> findByOrganizationId(int organizationId);

    @Modifying
    @Transactional
    Long deleteByVolunteeringId(int volunteeringId);
}
