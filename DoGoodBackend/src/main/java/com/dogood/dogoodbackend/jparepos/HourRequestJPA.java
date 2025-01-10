package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.volunteerings.Volunteering;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ApprovedHours;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequests;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.UserVolunteerDateKT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HourRequestJPA extends JpaRepository<HourApprovalRequests, UserVolunteerDateKT> {
    List<HourApprovalRequests> findByUserIdAndVolunteeringId(String userId, int volunteeringId);
    List<HourApprovalRequests> findByVolunteeringId(int volunteeringId);
}
