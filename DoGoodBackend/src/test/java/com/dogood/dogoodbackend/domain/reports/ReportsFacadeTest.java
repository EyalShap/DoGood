package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.domain.externalAIAPI.ProxyKeywordExtractor;
import com.dogood.dogoodbackend.domain.organizations.*;
import com.dogood.dogoodbackend.domain.posts.MemoryVolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.posts.PostsFacade;
import com.dogood.dogoodbackend.domain.posts.VolunteeringPostRepository;
import com.dogood.dogoodbackend.domain.users.MemoryUserRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.MemorySchedulingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportsFacadeTest {
    private ReportsFacade reportsFacade;
    private int organizationId, volunteeringId, postId, reportId;
    private final String actor = "TheDoctor";
    private VolunteeringPostRepository volunteeringPostRepository;
    private RequestRepository requestRepository;
    private OrganizationRepository organizationRepository;
    private VolunteeringRepository volunteeringRepository;
    private ReportRepository reportRepository;
    private VolunteeringFacade volunteeringFacade;
    private OrganizationsFacade organizationsFacade;
    private PostsFacade postsFacade;

    @BeforeEach
    void setUp() {
        this.volunteeringPostRepository = new MemoryVolunteeringPostRepository();
        this.requestRepository = new MemoryRequestRepository();
        this.organizationRepository = new MemoryOrganizationRepository();
        this.volunteeringRepository = new MemoryVolunteeringRepository();
        this.reportRepository = new MemoryReportRepository();
        UsersFacade usersFacade = new UsersFacade(new MemoryUserRepository(), new AuthFacade());

        this.organizationsFacade = new OrganizationsFacade(usersFacade,organizationRepository, requestRepository);
        this.volunteeringFacade = new VolunteeringFacade(usersFacade,organizationsFacade, volunteeringRepository, new MemorySchedulingManager());
        this.postsFacade = new PostsFacade(usersFacade,volunteeringPostRepository, volunteeringFacade, organizationsFacade, new ProxyKeywordExtractor());
        this.reportsFacade = new ReportsFacade(usersFacade,reportRepository, postsFacade);

        this.postsFacade.setReportsFacade(reportsFacade);
        this.organizationsFacade.setVolunteeringFacade(volunteeringFacade);

        this.organizationId = this.organizationsFacade.createOrganization("Organization", "Description", "0547960995", "org@gmail.com", actor);
        this.volunteeringId = this.volunteeringFacade.createVolunteering(actor, organizationId, "Volunteering", "Description");
        this.postId = this.postsFacade.createVolunteeringPost("Title", "Description", actor, volunteeringId);
        this.reportId = this.reportsFacade.createReport(actor, postId, "Description");
    }

    @Test
    void givenValidFields_whenCreateReport() {

    }

    @Test
    void removeReport() {
    }

    @Test
    void editReport() {
    }

    @Test
    void getReport() {
    }

    @Test
    void getAllReportDTOs() {
    }

    @Test
    void removePostReports() {
    }
}