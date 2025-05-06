package com.dogood.dogoodbackend.domain.posts;

import com.dogood.dogoodbackend.domain.chat.DatabaseMessageRepository;
import com.dogood.dogoodbackend.domain.externalAIAPI.*;
import com.dogood.dogoodbackend.domain.organizations.DBOrganizationRepository;
import com.dogood.dogoodbackend.domain.organizations.OrganizationRepository;
import com.dogood.dogoodbackend.domain.organizations.OrganizationsFacade;
import com.dogood.dogoodbackend.domain.reports.*;
import com.dogood.dogoodbackend.domain.requests.DBRequestRepository;
import com.dogood.dogoodbackend.domain.requests.RequestRepository;
import com.dogood.dogoodbackend.domain.users.DatabaseUserRepository;
import com.dogood.dogoodbackend.domain.users.UserRepository;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.DatabaseNotificationRepository;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.volunteerings.DatabaseVolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringFacade;
import com.dogood.dogoodbackend.domain.volunteerings.VolunteeringRepository;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.DatabaseSchedulingManager;
import com.dogood.dogoodbackend.jparepos.*;
import com.dogood.dogoodbackend.service.FacadeManager;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.spy;

@SpringBootTest
public class PostsFacadeIntegrationTest extends AbstractPostsFacadeTest{

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FacadeManager facadeManager;

    @Override
    @AfterEach
    void tearDown() {
        applicationContext.getBean(UserJPA.class).deleteAll();
        applicationContext.getBean(VolunteeringJPA.class).deleteAll();
        applicationContext.getBean(OrganizationJPA.class).deleteAll();
        applicationContext.getBean(VolunteeringPostJPA.class).deleteAll();
        applicationContext.getBean(VolunteerPostJPA.class).deleteAll();
        applicationContext.getBean(RequestJPA.class).deleteAll();
        applicationContext.getBean(ReportJPA.class).deleteAll();
        applicationContext.getBean(BannedJPA.class).deleteAll();
        applicationContext.getBean(HourRequestJPA.class).deleteAll();
        applicationContext.getBean(AppointmentJPA.class).deleteAll();
        applicationContext.getBean(MessageJPA.class).deleteAll();
        applicationContext.getBean(NotificationJPA.class).deleteAll();
    }

    @Override
    protected PostsFacade createPostsFacade() {
        return facadeManager.getPostsFacade();
    }

    @Override
    protected UsersFacade createUsersFacade() {
        return facadeManager.getUsersFacade();
    }

    @Override
    protected VolunteeringFacade createVolunteeringFacade() {
        return facadeManager.getVolunteeringFacade();
    }

    @Override
    protected VolunteeringFacade createSpyVolunteeringFacade() {
        return spy(facadeManager.getVolunteeringFacade());
    }

    @Override
    protected OrganizationsFacade createOrganizationsFacade() {
        return facadeManager.getOrganizationsFacade();
    }

    @Override
    protected OrganizationsFacade createSpyOrganizationsFacade() {
        return spy(facadeManager.getOrganizationsFacade());
    }

    @Override
    protected ReportsFacade createReportsFacade() {
        return facadeManager.getReportsFacade();
    }

    @Override
    protected ReportsFacade createSpyReportsFacade() {
        return spy(facadeManager.getReportsFacade());
    }

    @Override
    protected NotificationSystem createNotificationSystem() {
        return facadeManager.getNotificationSystem();
    }

    /*@Override
    protected KeywordExtractor createSpyKeywordExtractor() {
        return spy(facadeManager.getPostsFacade().getKeywordExtractor());
    }*/

    /*@Override
    protected VolunteeringPostRepository createVolunteeringPostRepository() {
        DBVolunteeringPostRepository repo = new DBVolunteeringPostRepository();
        volunteeringPostJPA = applicationContext.getBean(VolunteeringPostJPA.class);
        repo.setJPA(volunteeringPostJPA);
        return repo;
    }

    @Override
    protected VolunteerPostRepository createVolunteerPostRepository() {
        DBVolunteerPostRepository repo = new DBVolunteerPostRepository();
        volunteerPostJPA = applicationContext.getBean(VolunteerPostJPA.class);
        repo.setJPA(volunteerPostJPA);
        return repo;
    }

    @Override
    protected UsersFacade createUsersFacade() {
        userJPA = applicationContext.getBean(UserJPA.class);
        UserRepository userRepository = new DatabaseUserRepository(userJPA);
        return new UsersFacade(userRepository, new AuthFacade(), null);
    }

    @Override
    protected VolunteeringFacade createVolunteeringFacade() {
        VolunteeringRepository volunteeringRepository = new DatabaseVolunteeringRepository(applicationContext.getBean(VolunteeringJPA.class));
        return new VolunteeringFacade(this.usersFacade, this.organizationsFacade, volunteeringRepository, null, null);
    }

    @Override
    protected OrganizationsFacade createOrganizationsFacade() {
        OrganizationRepository organizationRepository = new DBOrganizationRepository(applicationContext.getBean(OrganizationJPA.class));
        RequestRepository requestRepository = new DBRequestRepository(applicationContext.getBean(RequestJPA.class));
        OrganizationsFacade organizationsFacade = new OrganizationsFacade(this.usersFacade, organizationRepository, requestRepository);
        return organizationsFacade;
    }

    @Override
    protected ReportsFacade createReportsFacade() {
        ReportRepository reportRepository = new DBReportRepository(applicationContext.getBean(ReportJPA.class));
        BannedRepository bannedRepository = new DBBannedRepository(applicationContext.getBean(BannedJPA.class));
        return new ReportsFacade(this.usersFacade, reportRepository, bannedRepository, postsFacade, volunteeringFacade, organizationsFacade);
    }

    @Override
    protected KeywordExtractor createKeywordExtractor() {
        return new AIKeywordExtractor(ai);
    }

    @Override
    protected SkillsAndCategoriesExtractor createSkillsAndCategoriesExtractor() {
        return new AISkillsAndCategoriesExtractor(ai);
    }

    @Override
    protected RequestRepository createRequestRepository() {
        return new DBRequestRepository(applicationContext.getBean(RequestJPA.class));
    }

    @Override
    protected NotificationSystem createNotificationSystem() {
        return Mockito.mock(NotificationSystem.class);
    }*/
}
