package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.UserVolunteerDateKT;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface HourRequestJPA extends JpaRepository<HourApprovalRequest, UserVolunteerDateKT> {
    List<HourApprovalRequest> findByUserIdAndVolunteeringId(String userId, int volunteeringId);
    List<HourApprovalRequest> findByVolunteeringId(int volunteeringId);
    List<HourApprovalRequest> findByUserIdAndApproved(String userId, boolean approved);
    Long deleteByVolunteeringId(int volunteeringId);
    Long deleteByUserId(String userId);
    Long deleteByVolunteeringIdAndApproved(int volunteeringId, boolean approved);
    Long deleteByUserIdAndVolunteeringIdAndApproved(String userId, int volunteeringId, boolean approved);
}
