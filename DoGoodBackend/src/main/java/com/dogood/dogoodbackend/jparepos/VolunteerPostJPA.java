package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.posts.VolunteerPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VolunteerPostJPA extends JpaRepository<VolunteerPost, Integer> {
}