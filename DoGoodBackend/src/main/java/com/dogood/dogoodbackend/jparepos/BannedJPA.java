package com.dogood.dogoodbackend.jparepos;

import com.dogood.dogoodbackend.domain.reports.Banned;
import com.dogood.dogoodbackend.domain.reports.ReportObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannedJPA extends JpaRepository<Banned, String> {
}
