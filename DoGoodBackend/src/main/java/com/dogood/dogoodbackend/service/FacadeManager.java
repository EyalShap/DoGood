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
import com.dogood.dogoodbackend.domain.volunteerings.*;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.MemorySchedulingManager;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingManager;

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
    //private UsersFacade;

    public FacadeManager(VolunteeringRepository volRepo, OrganizationRepository orgRepo, VolunteeringPostRepository volPostRepo,
                              RequestRepository reqRepo, ReportRepository repRepo, SchedulingManager schedMan, KeywordExtractor keyExt){
        this.organizationsFacade = new OrganizationsFacade(orgRepo, reqRepo);
        this.volunteeringFacade = new VolunteeringFacade(this.organizationsFacade, volRepo, schedMan);
        this.postsFacade = new PostsFacade(volPostRepo, volunteeringFacade, organizationsFacade, keyExt);
        this.reportsFacade = new ReportsFacade(repRepo, postsFacade);

        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);
    }

    public FacadeManager() {
        OrganizationRepository orgRepo = new MemoryOrganizationRepository();
        RequestRepository reqRepo = new MemoryRequestRepository();
        VolunteeringRepository volRepo = new MemoryVolunteeringRepository();
        SchedulingManager schedMan = new MemorySchedulingManager();
        VolunteeringPostRepository volPostRepo = new MemoryVolunteeringPostRepository();
        ReportRepository repRepo = new MemoryReportRepository();
        KeywordExtractor keyExt = new ProxyKeywordExtractor();

        this.organizationsFacade = new OrganizationsFacade(orgRepo, reqRepo);
        this.volunteeringFacade = new VolunteeringFacade(this.organizationsFacade, volRepo, schedMan);
        this.postsFacade = new PostsFacade(volPostRepo, volunteeringFacade, organizationsFacade, keyExt);
        this.reportsFacade = new ReportsFacade(repRepo, postsFacade);

        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);
    }

    public void createFacades(VolunteeringRepository volRepo, OrganizationRepository orgRepo, VolunteeringPostRepository volPostRepo,
                              RequestRepository reqRepo, ReportRepository repRepo, SchedulingManager schedMan, KeywordExtractor keyExt){
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
}
