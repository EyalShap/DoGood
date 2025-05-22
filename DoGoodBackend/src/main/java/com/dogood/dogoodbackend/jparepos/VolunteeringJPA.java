package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.volunteerings.Volunteering;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VolunteeringJPA extends JpaRepository<Volunteering, Integer> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Volunteering> findById(int id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Volunteering v WHERE v.id = :id")
    Optional<Volunteering> findAndWriteById(int id);
}
