package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.posts.VolunteerPost;
import com.dogood.dogoodbackend.domain.reports.Report;
import com.dogood.dogoodbackend.domain.reports.ReportKey;
import com.dogood.dogoodbackend.domain.reports.ReportObject;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportJPA extends JpaRepository<Report, ReportKey> {
    //@Modifying
    //@Transactional
    //@Query("DELETE FROM Report WHERE reportedPostId = :reportedPostId")
    //void deletePostId(@Param("reportedPostId") int reportedPostId);

    @Modifying
    @Transactional
    Long deleteByReportedIdAndReportObject(String reportedId, ReportObject reportObject);

    List<Report> findAllByReportObject(ReportObject reportObject);
}
