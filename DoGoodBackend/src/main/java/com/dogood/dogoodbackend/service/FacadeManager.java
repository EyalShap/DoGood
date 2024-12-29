package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.externalAIAPI.KeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.*;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.reports.ReportRepository;
import com.dogood.dogoodbackend.domain.reports.ReportsFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.SchedulingManager;

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
