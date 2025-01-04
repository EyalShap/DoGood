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
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.MemorySchedulingManager;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingManager;

public class FacadeManager {
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private PostsFacade postsFacade;
    private ReportsFacade reportsFacade;
    private UsersFacade usersFacade;
    private AuthFacade authFacade;

    public FacadeManager(VolunteeringRepository volRepo, OrganizationRepository orgRepo, VolunteeringPostRepository volPostRepo,
                         RequestRepository reqRepo, ReportRepository repRepo, UsersRepository userRepo, SchedulingManager schedMan, KeywordExtractor keyExt){
        this.organizationsFacade = new OrganizationsFacade(orgRepo, reqRepo);
        this.volunteeringFacade = new VolunteeringFacade(this.organizationsFacade, volRepo, schedMan);
        this.postsFacade = new PostsFacade(volPostRepo, volunteeringFacade, organizationsFacade, keyExt);
        this.reportsFacade = new ReportsFacade(repRepo, postsFacade);
        this.authFacade = new AuthFacade();
        this.usersFacade = new UsersFacade(userRepo, authFacade);

        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);
    }

    public FacadeManager() {
        OrganizationRepository orgRepo = new MemoryOrganizationRepository();
        RequestRepository reqRepo = new MemoryRequestRepository();
        VolunteeringRepository volRepo = new MemoryVolunteeringRepository();
        SchedulingManager schedMan = new MemorySchedulingManager();
        VolunteeringPostRepository volPostRepo = new MemoryVolunteeringPostRepository();
        ReportRepository repRepo = new MemoryReportRepository();
        UsersRepository userReop = new MemoryUsersRepository();
        KeywordExtractor keyExt = new ProxyKeywordExtractor();

        this.organizationsFacade = new OrganizationsFacade(orgRepo, reqRepo);
        this.volunteeringFacade = new VolunteeringFacade(this.organizationsFacade, volRepo, schedMan);
        this.postsFacade = new PostsFacade(volPostRepo, volunteeringFacade, organizationsFacade, keyExt);
        this.reportsFacade = new ReportsFacade(repRepo, postsFacade);
        this.authFacade = new AuthFacade();
        this.usersFacade = new UsersFacade(userReop, authFacade);

        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);
    }

    public void createFacades(VolunteeringRepository volRepo, OrganizationRepository orgRepo, VolunteeringPostRepository volPostRepo,
                              RequestRepository reqRepo, ReportRepository repRepo, UsersRepository userRep, SchedulingManager schedMan, KeywordExtractor keyExt){
        if(organizationsFacade == null) {
            this.organizationsFacade = new OrganizationsFacade(orgRepo, reqRepo);
        }
        if(volunteeringFacade == null) {
            this.volunteeringFacade = new VolunteeringFacade(this.organizationsFacade, volRepo, schedMan);
        }
        if(postsFacade == null) {
            this.postsFacade = new PostsFacade(volPostRepo, volunteeringFacade, organizationsFacade, keyExt);
        }
        if(reportsFacade == null) {
            this.reportsFacade = new ReportsFacade(repRepo, postsFacade);
        }
        if(authFacade == null) {
            this.authFacade = new AuthFacade();
        }
        if (usersFacade == null) {
            this.usersFacade = new UsersFacade(userRep, authFacade);
        }
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
