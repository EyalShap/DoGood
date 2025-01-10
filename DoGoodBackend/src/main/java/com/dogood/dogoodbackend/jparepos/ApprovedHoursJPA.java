package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ApprovedHours;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequests;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointment;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.UserVolunteerDateKT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovedHoursJPA extends JpaRepository<ApprovedHours, UserVolunteerDateKT> {
    List<ApprovedHours> findByUserIdAndVolunteeringId(String userId, int volunteeringId);
    List<ApprovedHours> findByVolunteeringId(int volunteeringId);
}
