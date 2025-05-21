package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.posts.VolunteerPost;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface VolunteerPostJPA extends JpaRepository<VolunteerPost, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM VolunteerPost o WHERE o.id = :id")
    Optional<VolunteerPost> findByIdForUpdate(@Param("id") int id);
}