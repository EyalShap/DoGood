package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.externalAIAPI.ProxyKeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.*;
import com.dogood.dogoodbackend.domain.posts.MemoryVolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.reports.MemoryReportRepository;
import com.dogood.dogoodbackend.domain.reports.ReportRepository;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.users.MemoryUsersRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.UsersRepository;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.MemorySchedulingManager;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingManager;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class FacadeManager {
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private PostsFacade postsFacade;
    private ReportsFacade reportsFacade;
    private UsersFacade usersFacade;
    private AuthFacade authFacade;

    public FacadeManager(String jwtSecretKey, VolunteeringRepository volRepo, OrganizationRepository orgRepo, VolunteeringPostRepository volPostRepo,
                         RequestRepository reqRepo, ReportRepository repRepo, UsersRepository userRepo, SchedulingManager schedMan, KeywordExtractor keyExt){
        this.authFacade = new AuthFacade(jwtSecretKey);
        this.usersFacade = new UsersFacade(userRepo, authFacade);
        this.organizationsFacade = new OrganizationsFacade(usersFacade, orgRepo, reqRepo);
        this.volunteeringFacade = new VolunteeringFacade(usersFacade, this.organizationsFacade, volRepo, schedMan);
        this.postsFacade = new PostsFacade(usersFacade, volPostRepo, volunteeringFacade, organizationsFacade, keyExt);
        this.reportsFacade = new ReportsFacade(usersFacade, repRepo, postsFacade);
        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);
        this.postsFacade.setReportsFacade(reportsFacade);
        this.volunteeringFacade.setPostsFacade(postsFacade);
    }

    public VolunteeringFacade getVolunteeringFacade() {
        return volunteeringFacade;
    }

    public OrganizationsFacade getOrganizationsFacade() {
        return organizationsFacade;
    }

    public PostsFacade getPostsFacade() {
        return postsFacade;
    }

    public ReportsFacade getReportsFacade() {
        return reportsFacade;
    }

    public AuthFacade getAuthFacade() {
        return authFacade;
    }

    public UsersFacade getUsersFacade() {
        return usersFacade;
    }
}
