package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.volunteerings.scheduling.ScheduleAppointment;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.UserVolunteerTimeKT;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface AppointmentJPA extends JpaRepository<ScheduleAppointment, UserVolunteerTimeKT> {
    List<ScheduleAppointment> findByUserIdAndVolunteeringId(String userId, int volunteeringId);
    List<ScheduleAppointment> findByVolunteeringId(int volunteeringId);
    Long deleteByVolunteeringId(int volunteeringId);
    Long deleteByUserId(String userId);
    Long deleteByUserIdAndVolunteeringId(String userId, int volunteeringId);
    Long deleteByVolunteeringIdAndRangeId(int volunteeringId, int rangeId);
}
