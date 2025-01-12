package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.posts.Post;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPost;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolunteeringPostJPA extends JpaRepository<VolunteeringPost, Integer> {
    List<VolunteeringPost> findByOrganizationId(int organizationId);

    @Modifying
    @Transactional
    Long deleteByVolunteeringId(int volunteeringId);
}
